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
        return str.substring(begin, str.length() + end).replace("\\\\", "\\").
                replace("\\n", "\n").replace("\\\"", "\"").
                replace("$$", "$");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
