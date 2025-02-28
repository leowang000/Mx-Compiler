package middleend;

import java.util.HashMap;
import java.util.HashSet;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;

public class UnusedFunctionRemover implements IRVisitor {
    private final HashSet<String> usedFuncSet_ = new HashSet<>();
    private HashMap<String, IRFuncDef> funcDefMap_ = new HashMap<>();

    @Override
    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        funcDefMap_ = node.funcDefMap_;
        funcDefMap_.get("main").accept(this);
        funcDefMap_.entrySet().removeIf(entry -> !usedFuncSet_.contains(entry.getKey()));
    }

    @Override
    public void visit(IRFuncDecl node) {}

    @Override
    public void visit(IRFuncDef node) {
        if (usedFuncSet_.contains(node.name_)) {
            return;
        }
        usedFuncSet_.add(node.name_);
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
        for (var inst : node.instList_) {
            inst.accept(this);
        }
    }

    @Override
    public void visit(IRAllocaInst node) {}

    @Override
    public void visit(IRBinaryInst node) {}

    @Override
    public void visit(IRBrInst node) {}

    @Override
    public void visit(IRCallInst node) {
        var funcDef = funcDefMap_.get(node.funcName_);
        if (funcDef != null) {
            funcDef.accept(this);
        }
    }

    @Override
    public void visit(IRGetElementPtrInst node) {}

    @Override
    public void visit(IRIcmpInst node) {}

    @Override
    public void visit(IRJumpInst node) {}

    @Override
    public void visit(IRLoadInst node) {}

    @Override
    public void visit(IRMoveInst node) {}

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {}

    @Override
    public void visit(IRStoreInst node) {}
}
