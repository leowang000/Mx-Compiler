package AST.expr;

import AST.ASTVisitor;
import util.Position;
import util.type.FuncType;

// If member_ is a member function of class_, then the type of MemberExpr is the return type of member_.
public class MemberExprNode extends ExprNode {
    public ExprNode class_;
    public String member_;
    public FuncType funcType_ = null;

    public MemberExprNode(Position pos, ExprNode clas, String member) {
        super(pos, true);
        class_ = clas;
        member_ = member;
    }

    public static boolean isMemberFunc(ExprNode expr) {
        return (expr instanceof MemberExprNode) && ((MemberExprNode) expr).funcType_ != null;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
