package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class ThisNode extends AtomExprNode {
    public ThisNode(Position pos) {
        super(pos, false);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
