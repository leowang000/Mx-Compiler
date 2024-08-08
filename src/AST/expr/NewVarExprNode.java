package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.Type;

public class NewVarExprNode extends ExprNode {
    public NewVarExprNode(Position pos, String name) {
        super(pos, new Type(name), false);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
