package AST.stmt;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class WhileStmtNode extends StmtNode {
    public ExprNode cond_;
    public ArrayList<StmtNode> body_;

    public WhileStmtNode(Position pos, ExprNode cond, StmtNode body) {
        super(pos);
        cond_ = cond;
        body_ = StmtNode.toStmtArray(body);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
