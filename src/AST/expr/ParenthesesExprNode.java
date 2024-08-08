package AST.expr;

import AST.ASTVisitor;
import util.Position;

public class ParenthesesExprNode extends ExprNode {
    public ExprNode expr_;

    public ParenthesesExprNode(Position pos, ExprNode expr) {
        super(pos, expr.type_, false);
        expr_ = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
