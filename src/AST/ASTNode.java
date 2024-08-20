package AST;

import util.Position;

public abstract class ASTNode {
    public Position pos_;

    public ASTNode(Position pos) {
        pos_ = pos;
    }

    public abstract void accept(ASTVisitor visitor);
}
