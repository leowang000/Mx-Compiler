package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class UnaryExprNode extends ExprNode {
    public String op_;
    public ExprNode expr_;

    public UnaryExprNode(Position pos, String op, ExprNode expr) {
        super(pos, false);
        op_ = op;
        expr_ = expr;
    }

    @Override
    public void checkAndInferType() {
        if (expr_.type_.equals(new Type("int"))) {
            if (op_.equals("++") || op_.equals("--")) {
                if (expr_.isLeftValue_) {
                    type_ = new Type("int");
                    return;
                }
            }
            else {
                type_ = new Type("void");
                return;
            }
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
