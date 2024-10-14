package backend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.value.var.IRLocalVar;
import asm.util.Register;

public class LinearScanRegAllocator {
    private static class Interval {
        public int start_, end_;

        public Interval(int start, int end) {
            start_ = start;
            end_ = end;
        }
    }

    private HashMap<IRInst, Integer> linearOrderMap_ = null;
    private HashMap<IRInst, HashSet<IRLocalVar>> useMap_ = null, defMap_ = null, inMap_ = null, outMap_ = null;
    private HashMap<IRInst, ArrayList<IRInst>> succMap_ = null, predMap_ = null;
    private HashMap<IRLocalVar, Interval> liveIntervalMap_ = null;
    HashSet<IRLocalVar> spilledVarSet_ = null;
    int maxFuncArgCnt_ = 0;
    private static final ArrayList<Register> regList_;

    static {
        regList_ = new ArrayList<>();
        for (int i = 2; i <= 6; i++) {
            regList_.add(new Register("t" + i));
        }
        for (int i = 0; i <= 11; i++) {
            regList_.add(new Register("s" + i));
        }
        for (int i = 7; i >= 0; i--) {
            regList_.add(new Register("a" + i));
        }
    }

    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        for (var block : node.body_) {
            block.insertMoveInst();
        }
        getLinearOrderAndMaxFuncArgCnt(node);
        buildInstGraph(node);
        executeLiveAnalysis(node);
        getLiveInterval(node);
        allocateRegister(node);
    }

    private void getLinearOrderAndMaxFuncArgCnt(IRFuncDef node) {
        linearOrderMap_ = new HashMap<>();
        maxFuncArgCnt_ = 0;
        ArrayList<IRBasicBlock> rpo = node.getRPO();
        int instId = 0;
        for (var block : rpo) {
            for (var inst : block.instList_) {
                linearOrderMap_.put(inst, instId++);
                if (inst instanceof IRCallInst) {
                    maxFuncArgCnt_ = Math.max(maxFuncArgCnt_, ((IRCallInst) inst).args_.size());
                }
            }
        }
    }

    private void buildInstGraph(IRFuncDef node) {
        succMap_ = new HashMap<>();
        predMap_ = new HashMap<>();
        for (int i = 0; i < node.body_.size(); i++) {
            IRBasicBlock block = node.body_.get(i);
            for (int j = 0; j < block.instList_.size(); j++) {
                IRInst inst = block.instList_.get(j);
                if (j < block.instList_.size() - 1) {
                    succMap_.put(inst, new ArrayList<>(List.of(block.instList_.get(j + 1))));
                    continue;
                }
                ArrayList<IRInst> succs = new ArrayList<>();
                if (inst instanceof IRRetInst && i < node.body_.size() - 1) {
                    succs.add(node.body_.get(i + 1).instList_.get(0));
                }
                else if (inst instanceof IRJumpInst) {
                    succs.add(((IRJumpInst) inst).destBlock_.instList_.get(0));
                }
                else if (inst instanceof IRBrInst) {
                    succs.add(((IRBrInst) inst).trueBlock_.instList_.get(0));
                    succs.add(((IRBrInst) inst).falseBlock_.instList_.get(0));
                }
                succMap_.put(inst, succs);
                predMap_.put(inst, new ArrayList<>());
            }
        }
        for (var entry : succMap_.entrySet()) {
            for (var succ : entry.getValue()) {
                predMap_.get(succ).add(entry.getKey());
            }
        }
    }

    private void executeLiveAnalysis(IRFuncDef node) {
        ArrayList<IRRetInst> exitList = new ArrayList<>();
        executeLiveAnalysisInit(node, exitList);
        HashSet<IRLocalVar> args = new HashSet<>(node.args_);
        while (true) {
            boolean changed = false;
            ArrayDeque<IRInst> queue = new ArrayDeque<>(exitList);
            while (!queue.isEmpty()) {
                IRInst inst = queue.poll();
                for (var pred : predMap_.get(inst)) {
                    queue.offer(pred);
                }
                HashSet<IRLocalVar> tmpOut = new HashSet<>();
                for (var succ : succMap_.get(inst)) {
                    tmpOut.addAll(inMap_.get(succ));
                }
                HashSet<IRLocalVar> tmpIn = new HashSet<>(tmpOut);
                tmpIn.removeAll(defMap_.get(inst));
                tmpIn.addAll(useMap_.get(inst));
                tmpIn.removeAll(args);
                if (tmpIn.size() != inMap_.get(inst).size() || tmpOut.size() != outMap_.get(inst).size()) {
                    changed = true;
                }
                inMap_.put(inst, tmpIn);
                outMap_.put(inst, tmpOut);
            }
            if (!changed) {
                break;
            }
        }
    }

    private void executeLiveAnalysisInit(IRFuncDef node, ArrayList<IRRetInst> exitList) {
        useMap_ = new HashMap<>();
        defMap_ = new HashMap<>();
        inMap_ = new HashMap<>();
        outMap_ = new HashMap<>();
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                useMap_.put(inst, inst.getUse());
                defMap_.put(inst, inst.getDef());
                inMap_.put(inst, new HashSet<>());
                outMap_.put(inst, new HashSet<>());
                if (inst instanceof IRRetInst) {
                    exitList.add((IRRetInst) inst);
                }
            }
        }
    }

    private void getLiveInterval(IRFuncDef node) {
        liveIntervalMap_ = new HashMap<>();
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                int instOrder = linearOrderMap_.get(inst);
                for (var localVar : inMap_.get(inst)) {
                    if (liveIntervalMap_.containsKey(localVar)) {
                        liveIntervalMap_.get(localVar).end_ = Math.max(liveIntervalMap_.get(localVar).end_, instOrder);
                    }
                    else {
                        liveIntervalMap_.put(localVar, new Interval(Integer.MAX_VALUE, instOrder));
                    }
                }
                for (var localVar : outMap_.get(inst)) {
                    if (liveIntervalMap_.containsKey(localVar)) {
                        liveIntervalMap_.get(localVar).start_ =
                            Math.min(liveIntervalMap_.get(localVar).start_, instOrder);
                    }
                    else {
                        liveIntervalMap_.put(localVar, new Interval(instOrder, 0));
                    }
                }
            }
        }
    }

    private void allocateRegister(IRFuncDef node) {
        int regCnt = 17 + Math.max(8 - node.args_.size(), 0);
        spilledVarSet_ = new HashSet<>();
        PriorityQueue<Map.Entry<IRLocalVar, Interval>> liveIntervals = new PriorityQueue<>((lhs, rhs) -> {
            int compareStart = Integer.compare(lhs.getValue().start_, rhs.getValue().start_);
            if (compareStart != 0) {
                return compareStart;
            }
            return Integer.compare(rhs.getValue().end_, lhs.getValue().end_);
        });
        liveIntervals.addAll(liveIntervalMap_.entrySet());
        TreeSet<Map.Entry<IRLocalVar, Interval>> active = new TreeSet<>(
            Comparator.comparingInt((Map.Entry<IRLocalVar, Interval> entry) -> entry.getValue().end_)
                .thenComparingInt(entry -> entry.getValue().start_));
        HashSet<Register> free = new HashSet<>();
        for (int i = 0; i < regCnt; i++) {
            free.add(regList_.get(i));
        }
        HashSet<Register> used = new HashSet<>();
        while (!liveIntervals.isEmpty()) {
            Map.Entry<IRLocalVar, Interval> cur = liveIntervals.poll();
            Iterator<Map.Entry<IRLocalVar, Interval>> iter = active.iterator();
            while (iter.hasNext()) {
                Map.Entry<IRLocalVar, Interval> entry = iter.next();
                if (entry.getValue().end_ > cur.getValue().start_) {
                    break;
                }
                free.add(entry.getKey().register_);
                iter.remove();
            }
            if (active.size() < regCnt) {
                cur.getKey().register_ = free.iterator().next();
                used.add(cur.getKey().register_);
                free.remove(cur.getKey().register_);
                active.add(cur);
                return;
            }
            Map.Entry<IRLocalVar, Interval> last = active.last();
            if (last.getValue().end_ <= cur.getValue().end_) {
                cur.getKey().stackOffset_ = node.stackSize_;
                node.stackSize_ += 4;
                spilledVarSet_.add(cur.getKey());
            }
            else {
                cur.getKey().register_ = last.getKey().register_;
                last.getKey().register_ = null;
                last.getKey().stackOffset_ = node.stackSize_;
                node.stackSize_ += 4;
                spilledVarSet_.add(last.getKey());
                active.add(cur);
                active.remove(last);
            }
        }
        node.stackSize_ = (4 * maxFuncArgCnt_ + 4 * used.size() + 4 + node.stackSize_ + 15) / 16 * 16;
        for (var spilledVar : spilledVarSet_) {
            spilledVar.stackOffset_ += 4 * Math.max(maxFuncArgCnt_ - 8, 0);
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                node.args_.get(i).register_ = regList_.get(24 - i);
            }
            else {
                node.args_.get(i).stackOffset_ = node.stackSize_ + 4 * i;
            }
        }
        int cnt = 0;
        for (var usedReg : used) {
            if (usedReg.name_.charAt(0) == 's') {
                node.usedSRegisterMap_.put(usedReg, 4 * maxFuncArgCnt_ + 4 * (cnt++));
            }
        }
    }
}