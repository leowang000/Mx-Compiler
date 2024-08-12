package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class WhileStmtNode extends StmtNode {
    public ExprNode cond_;
    public SuiteStmtNode body_;

    public WhileStmtNode(Position pos, ExprNode cond, StmtNode body) {
        super(pos);
        cond_ = cond;
        body_ = (body == null ? null : new SuiteStmtNode(body));
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
