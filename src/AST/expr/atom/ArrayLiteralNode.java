package AST.expr.atom;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class ArrayLiteralNode extends AtomExprNode {
    public ArrayList<AtomExprNode> elemList_;

    public ArrayLiteralNode(Position pos, Type type) {
        super(pos, type, false);
        elemList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
