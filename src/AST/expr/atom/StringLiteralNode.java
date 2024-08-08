package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class StringLiteralNode extends AtomExprNode {
    public String value_;

    public StringLiteralNode(Position pos, String value) {
        super(pos, new Type("string"), false);
        value_ = value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
