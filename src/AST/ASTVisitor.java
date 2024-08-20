package AST;

import AST.module.*;
import AST.stmt.*;
import AST.expr.*;
import AST.expr.atom.*;

public interface ASTVisitor {
    void visit(ProgramNode node);

    void visit(ClassDefNode node);
    void visit(ConstructorDefNode node);
    void visit(FuncDefNode node);
    void visit(VarDefNode node);

    void visit(BreakStmtNode node);
    void visit(ContinueStmtNode node);
    void visit(ExprStmtNode node);
    void visit(ForStmtNode node);
    void visit(IfStmtNode node);
    void visit(ReturnStmtNode node);
    void visit(SuiteStmtNode node);
    void visit(VarDefStmtNode node);
    void visit(WhileStmtNode node);

    void visit(ArrayExprNode node);
    void visit(AssignExprNode node);
    void visit(BinaryExprNode node);
    void visit(ConditionalExprNode node);
    void visit(FuncCallExprNode node);
    void visit(MemberExprNode node);
    void visit(NewArrayExprNode node);
    void visit(NewVarExprNode node);
    void visit(ParenthesesExprNode node);
    void visit(PreUnaryExprNode node);
    void visit(UnaryExprNode node);

    void visit(ArrayLiteralNode node);
    void visit(BoolLiteralNode node);
    void visit(FStringNode node);
    void visit(IdentifierNode node);
    void visit(IntLiteralNode node);
    void visit(NullLiteralNode node);
    void visit(StringLiteralNode node);
    void visit(ThisNode node);
}
