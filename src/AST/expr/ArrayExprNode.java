package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class ArrayExprNode extends ExprNode {
    public ExprNode array_, index_;

    public ArrayExprNode(Position pos, ExprNode array, ExprNode index) {
        super(pos, true);
        array_ = array;
        index_ = index;
    }

    @Override
    public void checkAndInferType() {
        if (MemberExprNode.isMemberFunc(array_) || MemberExprNode.isMemberFunc(index_)) {
            System.out.println("Invalid Type");
            throw new SemanticError("Type Mismatch Error", pos_);
        }
        if (array_.type_.isArray_ && index_.type_.equals(new Type("int"))) {
            type_ = new Type(array_.type_.name_, array_.type_.dim_ - 1);
            return;
        }
        if (!array_.type_.isArray_) {
            System.out.println("Dimension Out Of Bound");
        }
        if (!index_.type_.equals(new Type("int"))) {
            System.out.println("Invalid Type");
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
