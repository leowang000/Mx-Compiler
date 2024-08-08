package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class PreUnaryExprNode extends ExprNode {
    public String op_;
    public ExprNode expr_;

    public PreUnaryExprNode(Position pos, String op, ExprNode expr) {
        super(pos, new Type("int"), true);
        op_ = op;
        expr_ = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
