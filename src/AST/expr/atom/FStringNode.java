package AST.expr.atom;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class FStringNode extends AtomExprNode {
    public ArrayList<String> strList_;
    public ArrayList<ExprNode> exprList_;

    public FStringNode(Position pos) {
        super(pos, false);
        type_ = new Type("string");
        strList_ = new ArrayList<>();
        exprList_ = new ArrayList<>();
    }

    @Override
    public void checkAndInferType() {
        for (ExprNode expr : exprList_) {
            if (!expr.type_.equals(new Type("int")) && !expr.type_.equals(new Type("bool")) &&
                !expr.type_.equals(new Type("string"))) {
                System.out.println("Invalid Type");
                throw new SemanticError("Type Mismatch Error", pos_);
            }
        }
    }

    public static String getFString(String str, int begin, int end) {
        String tmp = str.substring(begin, str.length() + end).replace("$$", "$");
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
