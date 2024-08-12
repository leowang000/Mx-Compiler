package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class ParenthesesExprNode extends ExprNode {
    public ExprNode expr_;

    public ParenthesesExprNode(Position pos, ExprNode expr) {
        super(pos, false);
        expr_ = expr;
    }

    @Override
    public void checkAndInferType() {
        type_ = new Type(expr_.type_);
        isLeftValue_ = expr_.isLeftValue_;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
