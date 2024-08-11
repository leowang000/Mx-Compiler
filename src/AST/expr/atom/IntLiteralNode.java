package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class IntLiteralNode extends AtomExprNode {
    public int value_;

    public IntLiteralNode(Position pos, int value) {
        super(pos, false);
        type_ = new Type("int");
        value_ = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
