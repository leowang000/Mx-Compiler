package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class ArrayExprNode extends ExprNode {
    public ExprNode array_, index_;

    public ArrayExprNode(Position pos, ExprNode array, ExprNode index) {
        super(pos, new Type(array.type_.name_, array.type_.dim_ - 1), array.isLeftValue_);
        array_ = array;
        index_ = index;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
