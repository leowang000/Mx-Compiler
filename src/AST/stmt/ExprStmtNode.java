package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class ExprStmtNode extends StmtNode {
    public ExprNode expr_;

    public ExprStmtNode(Position pos, ExprNode expr) {
        super(pos);
        expr_ = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
