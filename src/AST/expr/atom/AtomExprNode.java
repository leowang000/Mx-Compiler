package AST.expr.atom;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;
import util.type.Type;

public abstract class AtomExprNode extends ExprNode {
    public AtomExprNode(Position pos, boolean isLeftValue) {
        super(pos, isLeftValue);
    }

    @Override
    public abstract void accept(ASTVisitor visitor);
}
