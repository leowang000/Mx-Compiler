package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class ForStmtNode extends StmtNode {
    public StmtNode init_, body_;
    public ExprNode cond_, step_;

    public ForStmtNode(Position pos, StmtNode init, ExprNode cond, ExprNode step, StmtNode body) {
        super(pos);
        init_ = init;
        cond_ = cond;
        step_ = step;
        body_ = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
