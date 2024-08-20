package AST.module;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;

public class ProgramNode extends ASTNode {
    public ArrayList<ASTNode> defList_;

    public ProgramNode(Position pos) {
        super(pos);
        defList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
