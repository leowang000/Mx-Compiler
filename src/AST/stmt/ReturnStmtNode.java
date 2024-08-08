package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class ReturnStmtNode extends StmtNode {
    public ExprNode expr_;

    public ReturnStmtNode(Position pos, ExprNode expr) {
        super(pos);
        expr_ = expr;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
