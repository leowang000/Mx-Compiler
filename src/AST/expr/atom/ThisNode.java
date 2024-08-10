package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class ThisNode extends AtomExprNode {
    public ThisNode(Position pos, Type type) {
        super(pos, type, false);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
