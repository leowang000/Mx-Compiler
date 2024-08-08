package AST.stmt;

import AST.ASTVisitor;
import util.Position;

public class BreakStmtNode extends StmtNode {
    public BreakStmtNode(Position pos) {
        super(pos);
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
