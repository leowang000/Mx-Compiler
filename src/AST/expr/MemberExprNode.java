package AST.expr;

import AST.ASTVisitor;
import AST.def.ClassDefNode;
import util.Position;

// If member_ is a member function of class_, then the type of MemberExpr is the return type of member_.
public class MemberExprNode extends ExprNode {
    public ClassDefNode class_;
    public String member_;
    public boolean isFunc_;

    public MemberExprNode(Position pos, ClassDefNode clas, String member, boolean isFunc) {
        super(pos, isFunc ? clas.funcDefMap_.get(member).returnType_ : clas.varDefMap_.get(member).type_,
              !isFunc);
        class_ = clas;
        member_ = member;
        isFunc_ = isFunc;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
