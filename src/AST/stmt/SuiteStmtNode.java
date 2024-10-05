package AST.stmt;

import java.util.ArrayList;
import java.util.List;

import AST.ASTVisitor;
import util.Position;

public class SuiteStmtNode extends StmtNode {
    public ArrayList<StmtNode> stmtList_ = new ArrayList<>();

    public SuiteStmtNode(Position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
