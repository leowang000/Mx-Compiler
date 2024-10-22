package middleend;

import java.util.*;

import IR.inst.IRCallInst;
import IR.inst.IRPhiInst;
import IR.module.*;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class InlineOptimizer {
    private static final int kMaxInlineLineCount = 35;
    private IRProgram irProgram_ = null;
    private HashSet<String> shouldInline_ = new HashSet<>();
    private int inlineCnt = 0;

    public void visit(IRProgram node) {
        irProgram_ = node;
        for (var funcDef : node.funcDefMap_.values()) {
            if (getLineCount(funcDef) < kMaxInlineLineCount) {
                shouldInline_.add(funcDef.name_);
            }
        }
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private int getLineCount(IRFuncDef node) {
        int lineCount = 0;
        for (var block : node.body_) {
            lineCount += block.instList_.size() + block.phiMap_.size();
        }
        return lineCount;
    }

    private void visit(IRFuncDef node) {
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                // TODO
            }
        }
    }

    private static class InlineInfo {
        public ArrayList<IRBasicBlock> blockList_ = new ArrayList<>();
        public IRBasicBlock inlineEndBlock_;

        public InlineInfo(String label, IRFuncDef belong) {
            inlineEndBlock_ = new IRBasicBlock(label, belong);
        }
    }

    private InlineInfo getInlineInfo(IRFuncDef inlineFunc, IRFuncDef belong, IRCallInst callInst, int cnt) {
        InlineInfo inlineInfo = new InlineInfo(String.format("inline_end.%d", cnt), belong);
        HashMap<IRLocalVar, IRValue> argMap = new HashMap<>();
        HashMap<IRLocalVar, IRLocalVar> localVarMap = new HashMap<>();
        HashMap<IRBasicBlock, IRBasicBlock> blockMap = new HashMap<>();
        for (int i = 0; i < callInst.args_.size(); i++) {
            argMap.put(inlineFunc.args_.get(i), callInst.args_.get(i));
        }
        for (var block : inlineFunc.body_) {
            IRBasicBlock newBlock = new IRBasicBlock(String.format("inline.%s.%d", block.label_, cnt), belong);
            inlineInfo.blockList_.add(newBlock);
            blockMap.put(block, newBlock);
            for (var inst : block.phiMap_.values()) {
                for (var def : inst.def_) {
                    if (!localVarMap.containsKey(def)) {
                        localVarMap.put(def, new IRLocalVar(String.format("inline.%s.%d", def.name_, cnt), def.type_));
                    }
                }
            }
            for (var inst : block.instList_) {
                for (var def : inst.def_) {
                    if (!localVarMap.containsKey(def)) {
                        localVarMap.put(def, new IRLocalVar(String.format("inline.%s.%d", def.name_, cnt), def.type_));
                    }
                }
            }
        }
        for (int i = 0; i < inlineFunc.body_.size(); i++) {
            IRBasicBlock block = inlineFunc.body_.get(i);
            IRBasicBlock newBlock = inlineInfo.blockList_.get(i);
            for (var entry : block.phiMap_.entrySet()) {
                IRPhiInst phiInst = entry.getValue();
                IRPhiInst newPhiInst = new IRPhiInst(localVarMap.get(phiInst.result_));
                for (var phiInfo : phiInst.info_.entrySet()) {
                    newPhiInst.info_.put(blockMap.get(phiInfo.getKey()),
                                         getSubstitution(phiInfo.getValue(), argMap, localVarMap));
                }
                newBlock.phiMap_.put(localVarMap.get(entry.getKey()), newPhiInst);
            }
            // TODO
        }
        return inlineInfo;
    }

    private IRValue getSubstitution(IRValue value, HashMap<IRLocalVar, IRValue> argMap,
                                    HashMap<IRLocalVar, IRLocalVar> localVarMap) {
        if (value instanceof IRLocalVar) {
            IRLocalVar localVar = (IRLocalVar) value;
            if (argMap.containsKey(localVar)) {
                return argMap.get(localVar);
            }
            if (localVarMap.containsKey(localVar)) {
                return localVarMap.get(localVar);
            }
        }
        return value;
    }
}
