package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class PreUnaryExprNode extends ExprNode {
    public String op_;
    public ExprNode expr_;

    public PreUnaryExprNode(Position pos, String op, ExprNode expr) {
        super(pos, false);
        op_ = op;
        expr_ = expr;
    }

    @Override
    public void checkAndInferType() {
        if (expr_.isLeftValue_ && expr_.type_.equals(new Type("int"))) {
            type_ = new Type("int");
            return;
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
