package IR;

import IR.inst.*;
import IR.module.*;

public interface IRVisitor {
    void visit(IRProgram node);

    void visit(IRFuncDecl node);
    void visit(IRFuncDef node);
    void visit(IRGlobalVarDef node);
    void visit(IRStringLiteralDef node);
    void visit(IRStructDef node);

    void visit(IRBasicBlock node);

    void visit(IRAllocaInst node);
    void visit(IRBinaryInst node);
    void visit(IRBrInst node);
    void visit(IRCallInst node);
    void visit(IRGetElementPtrInst node);
    void visit(IRIcmpInst node);
    void visit(IRJumpInst node);
    void visit(IRLoadInst node);
    void visit(IRMoveInst node);
    void visit(IRPhiInst node);
    void visit(IRRetInst node);
    void visit(IRStoreInst node);
}
