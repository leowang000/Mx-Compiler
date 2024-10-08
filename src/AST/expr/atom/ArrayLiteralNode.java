package AST.expr.atom;

import java.util.ArrayList;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class ArrayLiteralNode extends AtomExprNode {
    public ArrayList<AtomExprNode> elemList_ = new ArrayList<>();

    public ArrayLiteralNode(Position pos) {
        super(pos, false);
    }

    @Override
    public void checkAndInferType() {
        type_ = getType(this);
        if (type_ == null) {
            return;
        }
        if (!equalsType(type_)) {
            System.out.println("Invalid Type");
            throw new SemanticError("Type Mismatch Error", pos_);
        }
    }

    public boolean equalsType(Type type) {
        if (!type.isArray_) {
            return false;
        }
        for (var atomExpr : elemList_) {
            if (atomExpr instanceof ArrayLiteralNode) {
                if (!((ArrayLiteralNode) atomExpr).equalsType(new Type(type.name_, type.dim_ - 1))) {
                    return false;
                }
            }
            else {
                if (!atomExpr.type_.equals(new Type(type.name_, type.dim_ - 1))) {
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

    private static Type getType(ArrayLiteralNode node) {
        for (var elem : node.elemList_) {
            if (elem instanceof ArrayLiteralNode) {
                Type subType = getType((ArrayLiteralNode) elem);
                if (subType != null) {
                    return new Type(subType.name_, subType.dim_ + 1);
                }
            }
            else {
                if (!(elem instanceof NullLiteralNode)) {
                    return new Type(elem.type_.name_, 1);
                }
            }
        }
        return null;
    }
}
