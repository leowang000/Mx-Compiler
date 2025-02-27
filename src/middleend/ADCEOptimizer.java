package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.value.var.IRLocalVar;

public class ADCEOptimizer {
    public void visit(IRProgram node) {
        new DominatorTreeBuilder().visit(node, true);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        HashSet<IRInst> workList = new HashSet<>();
        HashMap<IRInst, IRBasicBlock> instBlockMap = new HashMap<>();
        HashSet<IRInst> liveInstSet = new HashSet<>();
        HashSet<IRBasicBlock> liveBlockSet = new HashSet<>();
        HashMap<IRLocalVar, IRInst> defInstMap = new HashMap<>();
        for (var block : node.body_) {
            for (var inst : block.phiMap_.values()) {
                inst.getUseAndDef();
                for (var def : inst.def_) {
                    defInstMap.put(def, inst);
                }
                instBlockMap.put(inst, block);
            }
            for (var inst : block.instList_) {
                inst.getUseAndDef();
                for (var def : inst.def_) {
                    defInstMap.put(def, inst);
                }
                instBlockMap.put(inst, block);
                if ((inst instanceof IRStoreInst) || (inst instanceof IRCallInst) || (inst instanceof IRRetInst)) {
                    workList.add(inst);
                }
            }
        }
        while (!workList.isEmpty()) {
            IRInst inst = workList.iterator().next();
            workList.remove(inst);
            liveInstSet.add(inst);
            IRBasicBlock belong = instBlockMap.get(inst);
            liveBlockSet.add(belong);
            if (inst instanceof IRPhiInst) {
                IRPhiInst phiInst = (IRPhiInst) inst;
                for (var block : phiInst.info_.keySet()) {
                    IRInst lastInst = block.instList_.get(block.instList_.size() - 1);
                    if (!liveInstSet.contains(lastInst) && !workList.contains(lastInst)) {
                        workList.add(lastInst);
                    }
                }
            }
            for (var block : belong.domFrontiers_) {
                IRInst lastInst = block.instList_.get(block.instList_.size() - 1);
                if (!liveInstSet.contains(lastInst) && !workList.contains(lastInst)) {
                    workList.add(lastInst);
                }
            }
            for (var use : inst.use_) {
                IRInst defInst = defInstMap.get(use);
                if (defInst != null && !liveInstSet.contains(defInst) && !workList.contains(defInst)) {
                    workList.add(defInst);
                }
            }
        }
        ArrayList<IRBasicBlock> newBody = new ArrayList<>();
        for (var block : node.body_) {
            if (!liveBlockSet.contains(block)) {
                continue;
            }
            newBody.add(block);
            block.phiMap_.entrySet().removeIf(entry -> !liveInstSet.contains(entry.getValue()));
            ArrayList<IRInst> newInstList = new ArrayList<>();
            for (int i = 0; i < block.instList_.size(); i++) {
                IRInst inst = block.instList_.get(i);
                if (i != block.instList_.size() - 1) {
                    if (liveInstSet.contains(inst)) {
                        newInstList.add(inst);
                    }
                    continue;
                }
                if (!liveInstSet.contains(inst)) {
                    IRBasicBlock dest = block.idom_;
                    while (!liveBlockSet.contains(dest)) {
                        dest = dest.idom_;
                    }
                    newInstList.add(new IRJumpInst(dest));
                }
                else if (inst instanceof IRBrInst) {
                    IRBrInst brInst = (IRBrInst) inst;
                    while (!liveBlockSet.contains(brInst.trueBlock_)) {
                        brInst.trueBlock_ = brInst.trueBlock_.idom_;
                    }
                    while (!liveBlockSet.contains(brInst.falseBlock_)) {
                        brInst.falseBlock_ = brInst.falseBlock_.idom_;
                    }
                    newInstList.add(brInst);
                }
                else {
                    newInstList.add(inst);
                }
            }
            block.instList_ = newInstList;
        }
        node.body_ = newBody;
    }
}
