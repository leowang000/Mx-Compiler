package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class AssignExprNode extends ExprNode {
    public ExprNode lhs_, rhs_;

    public AssignExprNode(Position pos, ExprNode lhs, ExprNode rhs) {
        super(pos, lhs.type_, true);
        lhs_ = lhs;
        rhs_ = rhs;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
