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
    private HashMap<IRInst, ArrayList<IRInst>> succMap_ = null, predMap_ = null;
    private HashMap<IRLocalVar, Interval> liveIntervalMap_ = null;
    HashSet<IRLocalVar> spilledVarSet_ = null;
    int maxCallLiveOutCnt_ = 0;
    public static final ArrayList<Register> regList_;

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
        ArrayList<IRBasicBlock> rpo = node.getRPO();
        int instId = 0;
        for (var block : rpo) {
            for (var inst : block.instList_) {
                linearOrderMap_.put(inst, instId++);
                if (inst instanceof IRCallInst) {
                    node.maxFuncArgCnt_ = Math.max(node.maxFuncArgCnt_, ((IRCallInst) inst).args_.size());
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
                predMap_.put(inst, new ArrayList<>());
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
        executeLiveAnalysisInitialization(node, exitList);
        HashSet<IRLocalVar> args = new HashSet<>(node.args_);
        while (true) {
            boolean changed = false;
            ArrayDeque<IRInst> queue = new ArrayDeque<>(exitList);
            HashSet<IRInst> visited = new HashSet<>(exitList);
            while (!queue.isEmpty()) {
                IRInst inst = queue.poll();
                for (var pred : predMap_.get(inst)) {
                    if (!visited.contains(pred)) {
                        visited.add(pred);
                        queue.offer(pred);
                    }
                }
                HashSet<IRLocalVar> tmpOut = new HashSet<>();
                for (var succ : succMap_.get(inst)) {
                    tmpOut.addAll(succ.in_);
                }
                HashSet<IRLocalVar> tmpIn = new HashSet<>(tmpOut);
                tmpIn.removeAll(inst.def_);
                tmpIn.addAll(inst.use_);
                tmpIn.removeAll(args); // live in and live out do not contain function arguments
                if (tmpIn.size() != inst.in_.size() || tmpOut.size() != inst.out_.size()) {
                    changed = true;
                }
                inst.in_ = tmpIn;
                inst.out_ = tmpOut;
            }
            if (!changed) {
                break;
            }
        }
    }

    private void executeLiveAnalysisInitialization(IRFuncDef node, ArrayList<IRRetInst> exitList) {
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                inst.getUse();
                inst.getDef();
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
                for (var localVar : inst.in_) {
                    if (liveIntervalMap_.containsKey(localVar)) {
                        liveIntervalMap_.get(localVar).end_ = Math.max(liveIntervalMap_.get(localVar).end_, instOrder);
                    }
                    else {
                        liveIntervalMap_.put(localVar, new Interval(Integer.MAX_VALUE, instOrder));
                    }
                }
                for (var localVar : inst.out_) {
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
        int regCnt = 17 + Math.max(8 - node.args_.size(), 0);
        HashSet<Register> free = new HashSet<>();
        for (int i = 0; i < regCnt; i++) {
            free.add(regList_.get(i));
        }
        HashSet<Register> usedSRegisters = new HashSet<>();
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
                if (cur.getKey().register_.name_.charAt(0) == 's') {
                    usedSRegisters.add(cur.getKey().register_);
                }
                free.remove(cur.getKey().register_);
                active.add(cur);
                continue;
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
        maxCallLiveOutCnt_ = 0;
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRCallInst) {
                    int cnt = 0;
                    for (var variable : inst.out_) {
                        if (variable != ((IRCallInst) inst).result_ && variable.register_ != null &&
                            variable.register_.name_.charAt(0) != 's') {
                            cnt++;
                        }
                    }
                    maxCallLiveOutCnt_ = Math.max(maxCallLiveOutCnt_, cnt);
                }
            }
        }
        node.callLiveOutBegin_ =
            4 * (Math.max(node.maxFuncArgCnt_ - 8, 0) + Math.min(node.args_.size(), 8) + usedSRegisters.size());
        node.stackSize_ = (node.callLiveOutBegin_ + 4 * maxCallLiveOutCnt_ + node.stackSize_ + 4 + 15) / 16 * 16;
        for (var spilledVar : spilledVarSet_) {
            spilledVar.stackOffset_ += node.callLiveOutBegin_ + 4 * maxCallLiveOutCnt_;
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                node.args_.get(i).register_ = regList_.get(24 - i);
            }
            else {
                node.args_.get(i).stackOffset_ = node.stackSize_ + 4 * (i - 8);
            }
        }
        int cnt = 0;
        for (var usedSRegister : usedSRegisters) {
            node.usedSRegisterMap_.put(usedSRegister, 4 * (Math.max(node.maxFuncArgCnt_ - 8, 0) +
                                                           Math.min(node.args_.size(), 8) + (cnt++)));
        }
    }
}