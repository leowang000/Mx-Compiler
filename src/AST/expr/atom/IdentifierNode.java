package AST.expr.atom;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class IdentifierNode extends AtomExprNode {
    public String name_;

    public IdentifierNode(Position pos, Type type, String name) {
        super(pos, type, true);
        name_ = name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
