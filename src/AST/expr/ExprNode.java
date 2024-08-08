package AST.expr;

import AST.ASTNode;
import util.Position;
import util.Type;

public abstract class ExprNode extends ASTNode {
    public Type type_;
    public boolean isLeftValue_;

    public ExprNode(Position pos, Type type, boolean isLeftValue) {
        super(pos);
        type_ = type;
        isLeftValue_ = isLeftValue;
    }
}
