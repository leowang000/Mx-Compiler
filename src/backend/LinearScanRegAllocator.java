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
    private HashMap<IRLocalVar, Interval> liveIntervalMap_ = null;
    int maxCallLiveOutCnt_ = 0;
    public static final ArrayList<Register> freeRegisters_;

    static {
        freeRegisters_ = new ArrayList<>();
        for (int i = 2; i <= 6; i++) {
            freeRegisters_.add(new Register("t" + i));
        }
        for (int i = 0; i <= 11; i++) {
            freeRegisters_.add(new Register("s" + i));
        }
        for (int i = 7; i >= 0; i--) {
            freeRegisters_.add(new Register("a" + i));
        }
    }

    public void visit(IRProgram node) {
        node.reset();
        new LiveAnalyzer().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        getLinearOrderAndMaxFuncArgCnt(node);
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
        HashSet<IRLocalVar> spilledVarSet = new HashSet<>();
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
            free.add(freeRegisters_.get(i));
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
                spilledVarSet.add(cur.getKey());
            }
            else {
                cur.getKey().register_ = last.getKey().register_;
                last.getKey().register_ = null;
                last.getKey().stackOffset_ = node.stackSize_;
                node.stackSize_ += 4;
                spilledVarSet.add(last.getKey());
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
        for (var spilledVar : spilledVarSet) {
            spilledVar.stackOffset_ += node.callLiveOutBegin_ + 4 * maxCallLiveOutCnt_;
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                node.args_.get(i).register_ = freeRegisters_.get(24 - i);
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