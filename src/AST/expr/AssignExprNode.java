package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class AssignExprNode extends ExprNode {
    public ExprNode lhs_, rhs_;

    public AssignExprNode(Position pos, ExprNode lhs, ExprNode rhs) {
        super(pos, true);
        lhs_ = lhs;
        rhs_ = rhs;
    }

    @Override
    public void checkAndInferType() {
        if (MemberExprNode.isMemberFunc(lhs_) || MemberExprNode.isMemberFunc(rhs_)) {
            throw new SemanticError("Type Mismatch Error", pos_);
        }
        if (lhs_.isLeftValue_ && lhs_.type_.equals(rhs_.type_)) {
            type_ = new Type(lhs_.type_);
            return;
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
