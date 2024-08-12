package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class NewVarExprNode extends ExprNode {
    public NewVarExprNode(Position pos, String typeName) {
        super(pos, true);
        type_ = new Type(typeName);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
