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
        String tmp = str.substring(1, str.length() - 1);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < tmp.length()) {
            char ch = tmp.charAt(i++);
            if (ch == '\\') {
                switch (tmp.charAt(i++)) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case '\"':
                        sb.append('\"');
                        break;
                }
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
