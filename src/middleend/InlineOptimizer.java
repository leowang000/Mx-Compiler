package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class InlineOptimizer {
    private final int kMaxInlineLineCount;
    private IRProgram irProgram_ = null;
    private final HashSet<String> shouldInline_ = new HashSet<>();
    private final HashMap<IRCallInst, InlineInfo> inlineInfoMap_ = new HashMap<>();
    private static int inlineCnt = 0;

    public InlineOptimizer(int maxInlineLineCount) {
        kMaxInlineLineCount = maxInlineLineCount;
    }

    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        irProgram_ = node;
        for (var funcDef : node.funcDefMap_.values()) {
            if (getLineCount(funcDef) < kMaxInlineLineCount) {
                shouldInline_.add(funcDef.name_);
            }
            for (var block : funcDef.body_) {
                for (var inst : block.phiMap_.values()) {
                    inst.getUseAndDef();
                }
                for (var inst : block.instList_) {
                    inst.getUseAndDef();
                }
            }
        }
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
        for (var funcDef : node.funcDefMap_.values()) {
            ArrayList<IRBasicBlock> newBody = new ArrayList<>();
            HashMap<IRBasicBlock, Integer> blockStartIndexMap = new HashMap<>();
            HashMap<IRBasicBlock, Integer> blockEndIndexMap = new HashMap<>();
            HashMap<IRBasicBlock, IRBasicBlock> blockMap_ = new HashMap<>();
            IRBasicBlock currentBlock;
            for (var block : funcDef.body_) {
                blockStartIndexMap.put(block, newBody.size());
                currentBlock = block;
                ArrayList<IRInst> instListCopy = new ArrayList<>(block.instList_);
                block.instList_.clear();
                for (var inst : instListCopy) {
                    if (!(inst instanceof IRCallInst) || !inlineInfoMap_.containsKey(inst)) {
                        currentBlock.instList_.add(inst);
                        continue;
                    }
                    InlineInfo inlineInfo = inlineInfoMap_.get(inst);
                    currentBlock.instList_.addAll(inlineInfo.blockList_.get(0).instList_);
                    newBody.add(currentBlock);
                    for (int i = 1; i < inlineInfo.blockList_.size(); i++) {
                        for (var phiInst : inlineInfo.blockList_.get(i).phiMap_.values()) {
                            IRValue value = phiInst.info_.get(inlineInfo.blockList_.get(0));
                            if (value != null) {
                                phiInst.info_.remove(inlineInfo.blockList_.get(0));
                                phiInst.info_.put(currentBlock, value);
                            }
                        }
                        newBody.add(inlineInfo.blockList_.get(i));
                    }
                    for (var phiInst : inlineInfo.inlineEnd_.phiMap_.values()) {
                        IRValue value = phiInst.info_.get(inlineInfo.blockList_.get(0));
                        if (value != null) {
                            phiInst.info_.remove(inlineInfo.blockList_.get(0));
                            phiInst.info_.put(currentBlock, value);
                        }
                    }
                    currentBlock = inlineInfo.inlineEnd_;
                }
                if (!currentBlock.phiMap_.isEmpty() || !currentBlock.instList_.isEmpty()) {
                    newBody.add(currentBlock);
                }
                blockEndIndexMap.put(block, newBody.size());
                if (currentBlock != block) {
                    blockMap_.put(block, newBody.get(newBody.size() - 1));
                }
            }
            funcDef.body_ = newBody;
            for (int i = 0; i < funcDef.body_.size(); i++) {
                IRBasicBlock block = funcDef.body_.get(i);
                for (var inst : block.phiMap_.values()) {
                    HashSet<IRBasicBlock> needSubstitutionBlockSet = new HashSet<>();
                    for (var phiBlock : inst.info_.keySet()) {
                        if (blockMap_.containsKey(phiBlock) &&
                            (i < blockStartIndexMap.get(phiBlock) || i >= blockEndIndexMap.get(phiBlock))) {
                            needSubstitutionBlockSet.add(phiBlock);
                        }
                    }
                    for (var needSubstitutionBlock : needSubstitutionBlockSet) {
                        IRValue value = inst.info_.get(needSubstitutionBlock);
                        inst.info_.remove(needSubstitutionBlock);
                        inst.info_.put(blockMap_.get(needSubstitutionBlock), value);
                    }
                }
            }
        }
        new CFGBuilder().visit(node);
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
                if (inst instanceof IRCallInst) {
                    IRCallInst callInst = (IRCallInst) inst;
                    if (shouldInline_.contains(callInst.funcName_)) {
                        InlineInfo inlineInfo =
                            getInlineInfo(irProgram_.funcDefMap_.get(callInst.funcName_), node, callInst, inlineCnt++);
                        inlineInfoMap_.put(callInst, inlineInfo);
                    }
                }
            }
        }
    }

    private static class InlineInfo {
        public ArrayList<IRBasicBlock> blockList_ = new ArrayList<>();
        public IRBasicBlock inlineEnd_;

        public InlineInfo(String label, IRFuncDef belong) {
            inlineEnd_ = new IRBasicBlock(label, belong);
        }
    }

    private InlineInfo getInlineInfo(IRFuncDef inlineFunc, IRFuncDef belong, IRCallInst callInst, int cnt) {
        InlineInfo inlineInfo = new InlineInfo(String.format("inline_end.%d", cnt), belong);
        if (callInst.result_ != null) {
            inlineInfo.inlineEnd_.phiMap_.put(callInst.result_, new IRPhiInst(callInst.result_));
        }
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
                IRPhiInst newPhiInst =
                    (IRPhiInst) getSubstitution(entry.getValue(), argMap, localVarMap, blockMap, inlineInfo.inlineEnd_);
                newBlock.phiMap_.put(localVarMap.get(entry.getValue().result_), newPhiInst);
            }
            for (var inst : block.instList_) {
                newBlock.instList_.add(getSubstitution(inst, argMap, localVarMap, blockMap, inlineInfo.inlineEnd_));
                if (inst instanceof IRRetInst) {
                    IRRetInst retInst = (IRRetInst) inst;
                    if (callInst.result_ != null) {
                        IRPhiInst returnValuePhi = inlineInfo.inlineEnd_.phiMap_.get(callInst.result_);
                        returnValuePhi.info_.put(newBlock, getSubstitution(retInst.value_, argMap, localVarMap));
                    }
                }
            }
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

    private IRInst getSubstitution(IRInst inst, HashMap<IRLocalVar, IRValue> argMap,
                                   HashMap<IRLocalVar, IRLocalVar> localVarMap,
                                   HashMap<IRBasicBlock, IRBasicBlock> blockMap, IRBasicBlock inlineEnd) {
        if (inst instanceof IRAllocaInst) {
            IRAllocaInst allocaInst = (IRAllocaInst) inst;
            return new IRAllocaInst(localVarMap.get(allocaInst.result_));
        }
        else if (inst instanceof IRBinaryInst) {
            IRBinaryInst binaryInst = (IRBinaryInst) inst;
            return new IRBinaryInst(localVarMap.get(binaryInst.result_),
                                    getSubstitution(binaryInst.lhs_, argMap, localVarMap),
                                    getSubstitution(binaryInst.rhs_, argMap, localVarMap), binaryInst.op_);
        }
        else if (inst instanceof IRBrInst) {
            IRBrInst brInst = (IRBrInst) inst;
            return new IRBrInst(getSubstitution(brInst.cond_, argMap, localVarMap), blockMap.get(brInst.trueBlock_),
                                blockMap.get(brInst.falseBlock_));
        }
        else if (inst instanceof IRCallInst) {
            IRCallInst callInst = (IRCallInst) inst;
            IRCallInst newCallInst = new IRCallInst(localVarMap.get(callInst.result_), callInst.funcName_);
            for (var arg : callInst.args_) {
                newCallInst.args_.add(getSubstitution(arg, argMap, localVarMap));
            }
            return newCallInst;
        }
        else if (inst instanceof IRGetElementPtrInst) {
            IRGetElementPtrInst getElementPtrInst = (IRGetElementPtrInst) inst;
            if (getElementPtrInst.id2_ == -1) {
                return new IRGetElementPtrInst(localVarMap.get(getElementPtrInst.result_),
                                               getSubstitution(getElementPtrInst.ptr_, argMap, localVarMap),
                                               getSubstitution(getElementPtrInst.id1_, argMap, localVarMap));
            }
            return new IRGetElementPtrInst(localVarMap.get(getElementPtrInst.result_),
                                           getSubstitution(getElementPtrInst.ptr_, argMap, localVarMap),
                                           getElementPtrInst.id2_);
        }
        else if (inst instanceof IRIcmpInst) {
            IRIcmpInst icmpInst = (IRIcmpInst) inst;
            return new IRIcmpInst(localVarMap.get(icmpInst.result_),
                                  getSubstitution(icmpInst.lhs_, argMap, localVarMap),
                                  getSubstitution(icmpInst.rhs_, argMap, localVarMap), icmpInst.cond_);
        }
        else if (inst instanceof IRJumpInst) {
            IRJumpInst jumpInst = (IRJumpInst) inst;
            return new IRJumpInst(blockMap.get(jumpInst.destBlock_));
        }
        else if (inst instanceof IRLoadInst) {
            IRLoadInst loadInst = (IRLoadInst) inst;
            return new IRLoadInst(localVarMap.get(loadInst.result_),
                                  getSubstitution(loadInst.pointer_, argMap, localVarMap));
        }
        else if (inst instanceof IRMoveInst) {
            IRMoveInst moveInst = (IRMoveInst) inst;
            return new IRMoveInst(localVarMap.get(moveInst.dest_), getSubstitution(moveInst.src_, argMap, localVarMap));
        }
        else if (inst instanceof IRPhiInst) {
            IRPhiInst phiInst = (IRPhiInst) inst;
            IRPhiInst newPhiInst = new IRPhiInst(localVarMap.get(phiInst.result_));
            for (var phiInfo : phiInst.info_.entrySet()) {
                newPhiInst.info_.put(blockMap.get(phiInfo.getKey()),
                                     getSubstitution(phiInfo.getValue(), argMap, localVarMap));
            }
            return newPhiInst;
        }
        else if (inst instanceof IRRetInst) {
            return new IRJumpInst(inlineEnd);
        }
        else if (inst instanceof IRStoreInst) {
            IRStoreInst storeInst = (IRStoreInst) inst;
            return new IRStoreInst(getSubstitution(storeInst.value_, argMap, localVarMap),
                                   getSubstitution(storeInst.pointer_, argMap, localVarMap));
        }
        return null;
    }
}
