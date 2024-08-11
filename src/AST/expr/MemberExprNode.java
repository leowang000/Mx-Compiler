package AST.expr;

import AST.ASTVisitor;
import AST.def.ClassDefNode;
import util.Position;
import util.type.FuncType;
import util.type.Type;

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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
