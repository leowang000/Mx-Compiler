package AST;

import util.Position;

abstract public class ASTNode {
    public Position pos_;

    public ASTNode(Position pos) {
        pos_ = pos;
    }

    abstract public void accept(ASTVisitor visitor);
}
