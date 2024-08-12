package AST.expr;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class FuncCallExprNode extends ExprNode {
    public ExprNode funcName_;
    public ArrayList<ExprNode> args_;

    public FuncCallExprNode(Position pos, ExprNode funcName) {
        super(pos, funcName.type_, false);
        funcName_ = funcName;
        args_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
