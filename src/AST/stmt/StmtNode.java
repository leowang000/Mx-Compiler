package AST.stmt;

import AST.ASTNode;
import util.Position;

public abstract class StmtNode extends ASTNode {
    StmtNode(Position pos) {
        super(pos);
    }
}
