package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class StringLiteralNode extends AtomExprNode {
    public String value_;

    public StringLiteralNode(Position pos, String value) {
        super(pos, false);
        type_ = new Type("string");
        value_ = value;
    }

    public static String getString(String str) {
        return str.substring(1, str.length() - 1).replace("\\\\", "\\").replace("\\n", "\n").replace("\\\"", "\"");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
