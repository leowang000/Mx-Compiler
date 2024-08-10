package AST.expr;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.atom.ArrayLiteralNode;
import util.Position;
import util.type.Type;

public class NewArrayExprNode extends ExprNode {
    public ArrayList<Integer> fixedSizeList_;
    public ArrayLiteralNode array_;

    public NewArrayExprNode(Position pos, Type type, ArrayLiteralNode array) {
        super(pos, type, false);
        fixedSizeList_ = new ArrayList<>();
        array_ = array;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
