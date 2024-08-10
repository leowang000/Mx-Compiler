package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.Type;

public class BinaryExprNode extends ExprNode {
    public ExprNode lhs_, rhs_;
    public String op_; // +-*/% << >> < > <= >= == != & && | ||

    BinaryExprNode(Position pos, ExprNode lhs, ExprNode rhs, String op) {
        super(pos, GetType(op), false);
        lhs_ = lhs;
        rhs_ = rhs;
        op_ = op;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    private static Type GetType(String op) {
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
            case "<<":
            case ">>":
            case "&":
            case "|":
                return new Type("int");
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "==":
            case "!=":
            case "&&":
            case "||":
                return new Type("bool");
        }
        return null;
    }
}
