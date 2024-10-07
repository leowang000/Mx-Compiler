package backend;

import java.util.ArrayList;
import java.util.HashMap;

import IR.inst.*;
import IR.module.*;

public class PhiResolver {
    private IRFuncDef curFuncDef_ = null;
    private HashMap<IRBasicBlock, ArrayList<IRBasicBlock>> splitBlockMap_ = null;
    private int splitBlockCnt_ = 0;

    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    public void visit(IRFuncDef node) {
        curFuncDef_ = node;
        splitBlockMap_ = new HashMap<>();
        for (var block : node.body_) {
            splitBlockMap_.put(block, new ArrayList<>());
        }
        for (var block : node.body_) {
            visit(block);
        }
        ArrayList<IRBasicBlock> newBody = new ArrayList<>();
        for (var block : node.body_) {
            newBody.add(block);
            newBody.addAll(splitBlockMap_.get(block));
        }
        node.body_ = newBody;
    }

    public void visit(IRBasicBlock node) {
        if (node.phiMap_.isEmpty()) {
            return;
        }
        HashMap<IRBasicBlock, IRBasicBlock> predMap = new HashMap<>();
        if (node.preds_.size() >= 2) {
            for (int i = 0; i < node.preds_.size(); i++) {
                IRBasicBlock pred = node.preds_.get(i);
                if (pred.succs_.size() == 1) {
                    continue;
                }
                IRBasicBlock block = new IRBasicBlock(String.format("split_%d", splitBlockCnt_++), curFuncDef_);
                splitBlockMap_.get(pred).add(block);
                block.instList_.add(new IRJumpInst(node));
                block.preds_.add(pred);
                block.succs_.add(node);
                pred.succs_.remove(node);
                pred.succs_.add(block);
                IRBrInst predLastInst = (IRBrInst) pred.instList_.get(pred.instList_.size() - 1);
                if (predLastInst.trueBlock_ == node) {
                    predLastInst.trueBlock_ = block;
                }
                if (predLastInst.falseBlock_ == node) {
                    predLastInst.falseBlock_ = block;
                }
                predMap.put(pred, block);
                node.preds_.set(i, block);
            }
        }
        for (var phiInst : node.phiMap_.values()) {
            for (var phiInfo : phiInst.info_.entrySet()) {
                IRBasicBlock pred =
                    (predMap.containsKey(phiInfo.getKey()) ? predMap.get(phiInfo.getKey()) : phiInfo.getKey());
                pred.moveList_.add(new IRMoveInst(phiInst.result_, phiInfo.getValue()));
            }
        }
        node.phiMap_ = new HashMap<>();
    }
}
