package backend;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import IR.type.IRPtrType;
import IR.value.var.IRLocalVar;
import asm.util.Register;

public class StackManager implements IRVisitor {
    private IRFuncDef belong_ = null;

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
        belong_ = node;
        for (var block : node.body_) {
            block.accept(this);
        }
        node.stackSize_ =
                (4 * Math.min(8, node.maxFuncArgCnt_) + 4 + node.stackSize_ + 4 * Math.max(node.maxFuncArgCnt_ - 8, 0) +
                 15) / 16 * 16;
        for (var localVar : node.localVarSet_) {
            localVar.stackOffset_ += 4 * Math.max(node.maxFuncArgCnt_ - 8, 0);
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                node.args_.get(i).register_ = "a" + i;
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
        belong_.maxFuncArgCnt_ = Math.max(belong_.maxFuncArgCnt_, node.args_.size());
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
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {}

    @Override
    public void visit(IRSelectInst node) {}

    @Override
    public void visit(IRStoreInst node) {}

    private void addLocalVar(IRLocalVar localVar, int size) {
        localVar.stackOffset_ = belong_.stackSize_;
        belong_.stackSize_ += size;
        belong_.localVarSet_.add(localVar);
    }
}
