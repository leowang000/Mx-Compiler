package AST.expr;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public abstract class ExprNode extends ASTNode {
    public Type type_;
    public boolean isLeftValue_;

    public ExprNode(Position pos, Type type, boolean isLeftValue) {
        super(pos);
        type_ = type;
        isLeftValue_ = isLeftValue;
    }

    @Override
    abstract public void accept(ASTVisitor visitor);
}
