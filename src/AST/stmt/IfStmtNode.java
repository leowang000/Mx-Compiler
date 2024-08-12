package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import parser.MxParser;
import util.Position;

public class IfStmtNode extends StmtNode {
    public ExprNode cond_;
    public SuiteStmtNode then_, else_;

    public IfStmtNode(Position pos, ExprNode cond, StmtNode then, StmtNode els) {
        super(pos);
        cond_ = cond;
        then_ = (then == null ? null : new SuiteStmtNode(then));
        else_ = (els == null ? null : new SuiteStmtNode(els));
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
