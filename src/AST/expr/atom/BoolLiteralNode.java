package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class BoolLiteralNode extends AtomExprNode {
    public boolean value_;

    public BoolLiteralNode(Position pos, boolean value) {
        super(pos, new Type("bool"), false);
        value_ = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
