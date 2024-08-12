package AST.stmt;

import java.util.ArrayList;
import java.util.List;

import AST.ASTVisitor;
import util.Position;

public class SuiteStmtNode extends StmtNode {
    public ArrayList<StmtNode> stmtList_;

    public SuiteStmtNode(Position pos) {
        super(pos);
        stmtList_ = new ArrayList<>();
    }

    public SuiteStmtNode(StmtNode stmt) {
        super(stmt.pos_);
        stmtList_ = (stmt instanceof SuiteStmtNode ? ((SuiteStmtNode) stmt).stmtList_ : new ArrayList<>(List.of(stmt)));
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
