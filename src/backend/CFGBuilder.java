package backend;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;

public class CFGBuilder implements IRVisitor {
    private IRBasicBlock currentBlock_ = null;

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
        for (var block : node.body_) {
            block.accept(this);
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
        currentBlock_ = node;
        node.instList_.get(node.instList_.size() - 1).accept(this);
    }

    @Override
    public void visit(IRAllocaInst node) {}

    @Override
    public void visit(IRBinaryInst node) {}

    @Override
    public void visit(IRBrInst node) {
        currentBlock_.succ_.add(node.trueBlock_);
        currentBlock_.succ_.add(node.falseBlock_);
        node.trueBlock_.pred_.add(currentBlock_);
        node.falseBlock_.pred_.add(currentBlock_);
    }

    @Override
    public void visit(IRCallInst node) {}

    @Override
    public void visit(IRGetElementPtrInst node) {}

    @Override
    public void visit(IRIcmpInst node) {}

    @Override
    public void visit(IRJumpInst node) {
        currentBlock_.succ_.add(node.destBlock_);
        node.destBlock_.pred_.add(currentBlock_);
    }

    @Override
    public void visit(IRLoadInst node) {}

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {}

    @Override
    public void visit(IRSelectInst node) {}

    @Override
    public void visit(IRStoreInst node) {}
}
