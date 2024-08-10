package AST.expr.atom;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;
import util.type.Type;

public abstract class AtomExprNode extends ExprNode {
    public AtomExprNode(Position pos, Type type, boolean isLeftValue) {
        super(pos, type, isLeftValue);
    }

    @Override
    abstract public void accept(ASTVisitor visitor);
}
