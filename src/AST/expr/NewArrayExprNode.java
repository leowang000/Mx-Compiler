package AST.expr;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.expr.atom.ArrayLiteralNode;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class NewArrayExprNode extends ExprNode {
    public ArrayList<ExprNode> fixedSizeList_;
    public ArrayLiteralNode array_;

    public NewArrayExprNode(Position pos, Type type, ArrayLiteralNode array) {
        super(pos, true);
        type_ = type;
        fixedSizeList_ = new ArrayList<>();
        array_ = array;
    }

    @Override
    public void checkAndInferType() {
        for (var expr : fixedSizeList_) {
            if (MemberExprNode.isMemberFunc(expr) || !expr.type_.equals(new Type("int"))) {
                System.out.println("Invalid Type");
                throw new SemanticError("Type Mismatch Error", pos_);
            }
        }
        if (array_ != null && !array_.equalsType(type_)) {
            System.out.println("Type Mismatch");
            throw new SemanticError("Type Mismatch Error", pos_);
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
