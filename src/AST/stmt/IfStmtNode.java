package AST.stmt;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Position;

public class IfStmtNode extends StmtNode {
    public ExprNode cond_;
    public StmtNode then_, else_;

    public IfStmtNode(Position pos, ExprNode cond, StmtNode then, StmtNode els) {
        super(pos);
        cond_ = cond;
        then_ = then;
        else_ = els;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
