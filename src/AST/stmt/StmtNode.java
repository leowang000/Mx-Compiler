package AST.stmt;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;

public abstract class StmtNode extends ASTNode {
    public StmtNode(Position pos) {
        super(pos);
    }

    @Override
    public abstract void accept(ASTVisitor visitor);

    public static ArrayList<StmtNode> toStmtArray(StmtNode stmt) {
        ArrayList<StmtNode> res = new ArrayList<>();
        if (stmt != null) {
            if (stmt instanceof SuiteStmtNode) {
                res = ((SuiteStmtNode) stmt).stmtList_;
            }
            else {
                res.add(stmt);
            }
        }
        return res;
    }
}
