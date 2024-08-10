package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class UnaryExprNode extends ExprNode {
    public String op_;
    public ExprNode expr_;

    public UnaryExprNode(Position pos, String op, ExprNode expr) {
        super(pos, new Type("int"), false);
        op_ = op;
        expr_ = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
