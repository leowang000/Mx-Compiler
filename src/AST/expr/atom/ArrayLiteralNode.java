package AST.expr.atom;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class ArrayLiteralNode extends AtomExprNode {
    public ArrayList<AtomExprNode> elemList_;

    public ArrayLiteralNode(Position pos) {
        super(pos, false);
        elemList_ = new ArrayList<>();
    }

    public boolean EqualsType(Type type) {
        if (!type.isArray_) {
            return false;
        }
        for (var atomExpr : elemList_) {
            if (atomExpr instanceof ArrayLiteralNode) {
                if (!((ArrayLiteralNode) atomExpr).EqualsType(new Type(type.name_, type.dim_ - 1))) {
                    return false;
                }
            }
            else {
                if (!atomExpr.type_.equals(type)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
