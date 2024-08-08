package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class IntLiteralNode extends AtomExprNode {
    public int value_;

    public IntLiteralNode(Position pos, int value) {
        super(pos, new Type("int"), false);
        value_ = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
