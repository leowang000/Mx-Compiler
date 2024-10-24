package backend.naive;

import java.util.HashSet;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import IR.type.IRPtrType;
import IR.value.var.IRLocalVar;
import asm.util.Register;

public class NaiveRegAllocator implements IRVisitor {
    private IRFuncDef curFuncDef_ = null;
    private HashSet<IRLocalVar> localVarSet_ = null;

    @Override
    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            funcDef.accept(this);
        }
    }

    @Override
    public void visit(IRFuncDecl node) {}

    @Override
    public void visit(IRFuncDef node) {
        curFuncDef_ = node;
        localVarSet_ = new HashSet<>();
        for (var block : node.body_) {
            block.accept(this);
        }
        node.stackSize_ =
            (4 * (Math.max(node.maxFuncArgCnt_ - 8, 0) + Math.min(node.args_.size(), 8)) + node.stackSize_ + 4 + 15) /
            16 * 16;
        for (var localVar : localVarSet_) {
            localVar.stackOffset_ += 4 * (Math.max(node.maxFuncArgCnt_ - 8, 0) + Math.min(node.args_.size(), 8));
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                node.args_.get(i).register_ = new Register("a" + i);
            }
            else {
                node.args_.get(i).stackOffset_ = node.stackSize_ + 4 * (i - 8);
            }
        }
    }

    @Override
    public void visit(IRGlobalVarDef node) {}

    @Override
    public void visit(IRStringLiteralDef node) {}

    @Override
    public void visit(IRStructDef node) {}

    @Override
    public void visit(IRBasicBlock node) {
        for (var inst : node.instList_) {
            inst.accept(this);
        }
        for (var inst : node.moveList_) {
            inst.accept(this);
        }
    }

    @Override
    public void visit(IRAllocaInst node) {
        addLocalVar(node.result_, ((IRPtrType) node.result_.type_).getDereferenceType().getSize());
    }

    @Override
    public void visit(IRBinaryInst node) {
        addLocalVar(node.result_, 4);
    }

    @Override
    public void visit(IRBrInst node) {}

    @Override
    public void visit(IRCallInst node) {
        curFuncDef_.maxFuncArgCnt_ = Math.max(curFuncDef_.maxFuncArgCnt_, node.args_.size());
        if (node.result_ != null) {
            addLocalVar(node.result_, 4);
        }
    }

    @Override
    public void visit(IRGetElementPtrInst node) {
        addLocalVar(node.result_, 4);
    }

    @Override
    public void visit(IRIcmpInst node) {
        addLocalVar(node.result_, 4);
    }

    @Override
    public void visit(IRJumpInst node) {}

    @Override
    public void visit(IRLoadInst node) {
        addLocalVar(node.result_, 4);
    }

    @Override
    public void visit(IRMoveInst node) {
        addLocalVar(node.dest_, 4);
    }

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {}

    @Override
    public void visit(IRStoreInst node) {}

    private void addLocalVar(IRLocalVar localVar, int size) {
        if (!localVarSet_.contains(localVar)) {
            localVar.stackOffset_ = curFuncDef_.stackSize_;
            curFuncDef_.stackSize_ += size;
            localVarSet_.add(localVar);
        }
    }
}
