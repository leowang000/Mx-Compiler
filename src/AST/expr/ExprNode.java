package AST.expr;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public abstract class ExprNode extends ASTNode {
    public Type type_ = null;
    public boolean isLeftValue_;

    public ExprNode(Position pos, boolean isLeftValue) {
        super(pos);
        isLeftValue_ = isLeftValue;
    }

    public void checkAndInferType() {}

    @Override
    abstract public void accept(ASTVisitor visitor);
}
