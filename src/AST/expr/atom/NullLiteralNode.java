package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class NullLiteralNode extends AtomExprNode {
    public NullLiteralNode(Position pos) {
        super(pos, new Type("null"), false);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
