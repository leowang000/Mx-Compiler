package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class ConditionalExprNode extends ExprNode {
    public ExprNode cond_, lhs_, rhs_;

    public ConditionalExprNode(Position pos, ExprNode cond, ExprNode lhs, ExprNode rhs) {
        super(pos, false);
        cond_ = cond;
        lhs_ = lhs;
        rhs_ = rhs;
    }

    @Override
    public void checkAndInferType() {
        if (MemberExprNode.isMemberFunc(cond_) || MemberExprNode.isMemberFunc(lhs_) ||
            MemberExprNode.isMemberFunc(rhs_)) {
            System.err.println("Type Mismatch");
            throw new SemanticError("Type Mismatch Error", pos_);
        }
        if (cond_.type_.equals(new Type("bool")) && lhs_.type_.equals(rhs_.type_)) {
            type_ = new Type(lhs_.type_.name_.equals("null") ? rhs_.type_ : lhs_.type_);
            return;
        }
        if (!cond_.type_.equals(new Type("bool"))) {
            System.err.println("Invalid Type");
        }
        if (!lhs_.type_.equals(new Type("bool"))) {
            System.err.println("Type Mismatch");
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}