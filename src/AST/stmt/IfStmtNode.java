package AST.stmt;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.ExprNode;
import parser.MxParser;
import util.Position;

public class IfStmtNode extends StmtNode {
    public ExprNode cond_;
    public ArrayList<StmtNode> then_, else_;

    public IfStmtNode(Position pos, ExprNode cond, StmtNode then, StmtNode els) {
        super(pos);
        cond_ = cond;
        then_ = StmtNode.toStmtArray(then);
        else_ = StmtNode.toStmtArray(els);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
