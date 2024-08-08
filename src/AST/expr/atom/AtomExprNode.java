package AST.expr.atom;

import AST.expr.ExprNode;
import util.Position;
import util.Type;

public abstract class AtomExprNode extends ExprNode {
    public AtomExprNode(Position pos, Type type, boolean isLeftValue) {
        super(pos, type, isLeftValue);
    }
}
