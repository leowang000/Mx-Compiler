package AST.expr;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;

public class FuncCallExprNode extends ExprNode {
    public ExprNode funcName_;
    public ArrayList<ExprNode> args_ = new ArrayList<>();

    public FuncCallExprNode(Position pos, ExprNode funcName) {
        super(pos, false);
        funcName_ = funcName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
