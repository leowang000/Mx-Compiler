package AST.expr.atom;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;
import util.type.Type;

public class FStringNode extends AtomExprNode {
    public ArrayList<String> strList_;
    public ArrayList<ExprNode> exprList_;

    public FStringNode(Position pos) {
        super(pos, new Type("string"), false);
        strList_ = new ArrayList<>();
        exprList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
