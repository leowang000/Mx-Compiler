package backend;

import java.util.*;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class AllocaEliminator implements IRVisitor {
    private HashMap<IRBasicBlock, HashMap<IRLocalVar, IRPhiInst>> blockPhiMap_;
    private HashMap<IRLocalVar, HashSet<IRBasicBlock>> defBlockMap_;
    private HashMap<IRLocalVar, Stack<IRValue>> allocaValueMap_;
    private HashMap<IRLocalVar, Integer> currentBlockDefCntMap_;
    private HashMap<IRLocalVar, IRValue> valueMap_;
    private ArrayList<IRInst> newInstList_;
    private int phi_cnt_ = 0;

    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            funcDef.accept(this);
        }
    }

    @Override
    public void visit(IRFuncDecl node) {}

    public void visit(IRFuncDef node) {
        initialize(node);
        getVarDefs(node);
        putPhi(node);
        node.body_.get(0).accept(this);
    }

    @Override
    public void visit(IRGlobalVarDef node) {}

    @Override
    public void visit(IRStringLiteralDef node) {}

    @Override
    public void visit(IRStructDef node) {}

    @Override
    public void visit(IRBasicBlock node) {
        currentBlockDefCntMap_ = new HashMap<>();
        newInstList_ = new ArrayList<>(blockPhiMap_.get(node).values());
        for (var inst : node.instList_) {
            inst.accept(this);
        }
        node.instList_ = newInstList_;
        for (var succ : node.succs_) {
            HashMap<IRLocalVar, IRPhiInst> succPhiMap = blockPhiMap_.get(succ);
            for (var entry : succPhiMap.entrySet()) {
                entry.getValue().info_.put(node, getCurrentValue(entry.getKey()));
            }
        }
        for (var child : node.domChildren_) {
            child.accept(this);
        }
        for (var entry : currentBlockDefCntMap_.entrySet()) {
            Stack<IRValue> stack = allocaValueMap_.get(entry.getKey());
            for (int i = 0; i < entry.getValue(); i++) {
                stack.pop();
            }
        }
    }

    @Override
    public void visit(IRAllocaInst node) {
        allocaValueMap_.put(node.result_, new Stack<>());
    }

    @Override
    public void visit(IRBinaryInst node) {
        node.lhs_ = getSubstitution(node.lhs_);
        node.rhs_ = getSubstitution(node.rhs_);
        newInstList_.add(node);
    }

    @Override
    public void visit(IRBrInst node) {
        newInstList_.add(node);
    }

    @Override
    public void visit(IRCallInst node) {
        node.args_.replaceAll(this::getSubstitution);
        newInstList_.add(node);
    }

    @Override
    public void visit(IRGetElementPtrInst node) {}

    @Override
    public void visit(IRIcmpInst node) {
        node.lhs_ = getSubstitution(node.lhs_);
        node.rhs_ = getSubstitution(node.rhs_);
        newInstList_.add(node);
    }

    @Override
    public void visit(IRJumpInst node) {
        newInstList_.add(node);
    }

    @Override
    public void visit(IRLoadInst node) {
        if (!isAllocaResult(node.pointer_)) {
            newInstList_.add(node);
            return;
        }
        valueMap_.put(node.result_, getCurrentValue(node.pointer_));
    }

    @Override
    public void visit(IRMoveInst node) {}

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {
        node.value_ = getSubstitution(node.value_);
        newInstList_.add(node);
    }

    @Override
    public void visit(IRSelectInst node) {}

    @Override
    public void visit(IRStoreInst node) {
        if (!isAllocaResult(node.pointer_)) {
            newInstList_.add(node);
            return;
        }
        IRLocalVar allocaResult = (IRLocalVar) node.pointer_;
        allocaValueMap_.get(allocaResult).push(node.value_);
        if (currentBlockDefCntMap_.containsKey((IRLocalVar) node.pointer_)) {
            currentBlockDefCntMap_.put(allocaResult, currentBlockDefCntMap_.get(allocaResult) + 1);
        }
        else {
            currentBlockDefCntMap_.put(allocaResult, 1);
        }
    }

    private void initialize(IRFuncDef node) {
        blockPhiMap_ = new HashMap<>();
        for (var block : node.body_) {
            blockPhiMap_.put(block, new HashMap<>());
        }
        defBlockMap_ = new HashMap<>();
        allocaValueMap_ = new HashMap<>();
        valueMap_ = new HashMap<>();
    }

    private void getVarDefs(IRFuncDef node) {
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRAllocaInst) {
                    IRAllocaInst allocaInst = (IRAllocaInst) inst;
                    defBlockMap_.put(allocaInst.result_, new HashSet<>());
                }
                if (inst instanceof IRStoreInst) {
                    IRStoreInst storeInst = (IRStoreInst) inst;
                    if (isAllocaResult(storeInst.pointer_)) {
                        defBlockMap_.get((IRLocalVar) storeInst.pointer_).add(block);
                    }
                }
            }
        }
    }

    private void putPhi(IRFuncDef node) {
        for (var entry : defBlockMap_.entrySet()) {
            IRLocalVar localVar = entry.getKey();
            for (var block : entry.getValue()) {
                putPhi(block, localVar);
            }
        }
    }

    private void putPhi(IRBasicBlock block, IRLocalVar localVar) {
        for (var boundary : block.domBoundary_) {
            HashMap<IRLocalVar, IRPhiInst> tmp = blockPhiMap_.get(boundary);
            if (!tmp.containsKey(localVar)) {
                IRLocalVar phiResult = new IRLocalVar(String.format("phi.%d", phi_cnt_++),
                                                      ((IRPtrType) localVar.type_).getDereferenceType());
                tmp.put(localVar, new IRPhiInst(phiResult, boundary));
                putPhi(boundary, localVar);
            }
        }
    }

    private boolean isAllocaResult(IRValue value) {
        return value instanceof IRLocalVar && ((IRLocalVar) value).isAllocaResult_;
    }

    private IRValue getCurrentValue(IRValue value) {
        if (!isAllocaResult(value)) {
            return null;
        }
        return allocaValueMap_.get((IRLocalVar) value).peek();
    }

    private IRValue getSubstitution(IRValue value) {
        if (value instanceof IRLocalVar) {
            IRLocalVar localVar = (IRLocalVar) value;
            if (valueMap_.containsKey(localVar)) {
                return valueMap_.get(localVar);
            }
        }
        return value;
    }
}
