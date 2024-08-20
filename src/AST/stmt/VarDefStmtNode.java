package AST.stmt;

import AST.ASTVisitor;
import AST.module.VarDefNode;
import util.Position;

public class VarDefStmtNode extends StmtNode {
    public VarDefNode varDef_;

    public VarDefStmtNode(Position pos, VarDefNode varDef) {
        super(pos);
        varDef_ = varDef;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
