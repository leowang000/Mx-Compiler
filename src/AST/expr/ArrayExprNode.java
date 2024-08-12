package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class ArrayExprNode extends ExprNode {
    public ExprNode array_, index_;

    public ArrayExprNode(Position pos, ExprNode array, ExprNode index) {
        super(pos, array.isLeftValue_);
        array_ = array;
        index_ = index;
    }

    @Override
    public void checkAndInferType() {
        if (MemberExprNode.isMemberFunc(array_) || MemberExprNode.isMemberFunc(index_)) {
            throw new SemanticError("Type Mismatch Error", pos_);
        }
        if (array_.type_.isArray_ && index_.type_.equals(new Type("int"))) {
            type_ = new Type(array_.type_.name_, array_.type_.dim_ - 1);
            return;
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
