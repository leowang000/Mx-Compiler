package AST.stmt;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class ForStmtNode extends StmtNode {
    public StmtNode init_;
    public ArrayList<StmtNode> body_ = new ArrayList<>();
    public ExprNode cond_, step_;

    public ForStmtNode(Position pos, StmtNode init, ExprNode cond, ExprNode step) {
        super(pos);
        init_ = init;
        cond_ = cond;
        step_ = step;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
