package AST.module;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;

public class ProgramNode extends ASTNode {
    public ArrayList<ASTNode> defList_ = new ArrayList<>();

    public ProgramNode(Position pos) {
        super(pos);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
