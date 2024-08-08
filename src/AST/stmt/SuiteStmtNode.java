package AST.stmt;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;

public class SuiteStmtNode extends StmtNode {
    public ArrayList<StmtNode> stmtList_;

    public SuiteStmtNode(Position pos) {
        super(pos);
        stmtList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
