package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class ParenthesesExprNode extends ExprNode {
    public ExprNode expr_;

    public ParenthesesExprNode(Position pos, ExprNode expr) {
        super(pos, false);
        type_ = new Type(expr.type_);
        expr_ = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
