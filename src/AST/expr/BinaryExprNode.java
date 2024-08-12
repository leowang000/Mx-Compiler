package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.error.SemanticError;
import util.type.Type;

public class BinaryExprNode extends ExprNode {
    public ExprNode lhs_, rhs_;
    public String op_; // +-*/% << >> < > <= >= == != & && | ||

    public BinaryExprNode(Position pos, ExprNode lhs, ExprNode rhs, String op) {
        super(pos, false);
        lhs_ = lhs;
        rhs_ = rhs;
        op_ = op;
    }

    @Override
    public void checkAndInferType() {
        if (MemberExprNode.isMemberFunc(lhs_) || MemberExprNode.isMemberFunc(rhs_)) {
            throw new SemanticError("Type Mismatch Error", pos_);
        }
        if (op_.equals("+") && lhs_.type_.equals(new Type("string")) &&
            rhs_.type_.equals(new Type("string"))) {
            type_ = new Type("string");
            return;
        }
        if (op_.equals("+") || op_.equals("-") || op_.equals("*") || op_.equals("/") || op_.equals("%") ||
            op_.equals("<<") || op_.equals(">>") || op_.equals("&") || op_.equals("|") || op_.equals("^")) {
            if (lhs_.type_.equals(new Type("int")) && rhs_.type_.equals(new Type("int"))) {
                type_ = new Type("int");
                return;
            }
        }
        if (op_.equals("<") || op_.equals("<=") || op_.equals(">") || op_.equals(">=")) {
            if ((lhs_.type_.equals(new Type("int")) && rhs_.type_.equals(new Type("int"))) ||
                (lhs_.type_.equals(new Type("string")) && rhs_.type_.equals(new Type("string")))) {
                type_ = new Type("bool");
                return;
            }
        }
        if (op_.equals("==") || op_.equals("!=")) {
            if (lhs_.type_.equals(rhs_.type_)) {
                type_ = new Type("bool");
                return;
            }
        }
        if (op_.equals("&&") || op_.equals("||")) {
            if (lhs_.type_.equals(new Type("bool")) && rhs_.type_.equals(new Type("bool"))) {
                type_ = new Type("bool");
                return;
            }
        }
        throw new SemanticError("Type Mismatch Error", pos_);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
