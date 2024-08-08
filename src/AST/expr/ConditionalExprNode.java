package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class ConditionalExprNode extends ExprNode {
    public ExprNode cond_, lhs_, rhs_;

    public ConditionalExprNode(Position pos, ExprNode cond, ExprNode lhs, ExprNode rhs) {
        super(pos, lhs.type_, false);
        cond_ = cond;
        lhs_ = lhs;
        rhs_ = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
