package AST.stmt;

import AST.ASTVisitor;
import util.Position;

public class ContinueStmtNode extends StmtNode {
    public ContinueStmtNode(Position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
