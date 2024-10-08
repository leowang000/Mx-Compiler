package AST.module;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.stmt.StmtNode;
import util.Position;

public class ConstructorDefNode extends ASTNode {
    public String name_;
    public ArrayList<StmtNode> stmtList_ = new ArrayList<>();

    public ConstructorDefNode(Position pos, String className) {
        super(pos);
        name_ = className;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
