package frontend;

import AST.ASTVisitor;
import AST.module.*;
import AST.expr.*;
import AST.expr.atom.*;
import AST.stmt.*;
import util.scope.ClassScope;
import util.scope.GlobalScope;
import util.type.*;

public class SymbolCollector implements ASTVisitor {
    private GlobalScope gScope_;

    public SymbolCollector(GlobalScope globalScope) {
        gScope_ = globalScope;
    }

    @Override
    public void visit(ProgramNode node) {
        for (var def : node.defList_) {
            def.accept(this);
        }
    }

    @Override
    public void visit(ClassDefNode node) {
        gScope_.addClass(node.name_, new ClassType(node), node.pos_);
        node.scope_ = new ClassScope(gScope_, node.name_);
        for (var funcDef : node.funcDefList_) {
            node.scope_.addFunc(funcDef.name_, new FuncType(funcDef), node.pos_);
        }
        for (var varDef : node.varDefList_) {
            for (var varDefUnit : varDef.varList_) {
                node.scope_.addVar(varDefUnit.first_, new Type(varDef.type_), node.pos_);
            }
        }
    }

    @Override
    public void visit(FuncDefNode node) {
        gScope_.addFunc(node.name_, new FuncType(node), node.pos_);
    }

    @Override
    public void visit(VarDefNode node) {}

    @Override
    public void visit(ConstructorDefNode node) {}

    @Override
    public void visit(BreakStmtNode node) {}

    @Override
    public void visit(ContinueStmtNode node) {}

    @Override
    public void visit(ExprStmtNode node) {}

    @Override
    public void visit(ForStmtNode node) {}

    @Override
    public void visit(IfStmtNode node) {}

    @Override
    public void visit(ReturnStmtNode node) {}

    @Override
    public void visit(SuiteStmtNode node) {}

    @Override
    public void visit(VarDefStmtNode node) {}

    @Override
    public void visit(WhileStmtNode node) {}

    @Override
    public void visit(ArrayExprNode node) {}

    @Override
    public void visit(AssignExprNode node) {}

    @Override
    public void visit(BinaryExprNode node) {}

    @Override
    public void visit(ConditionalExprNode node) {}

    @Override
    public void visit(FuncCallExprNode node) {}

    @Override
    public void visit(MemberExprNode node) {}

    @Override
    public void visit(NewArrayExprNode node) {}

    @Override
    public void visit(NewVarExprNode node) {}

    @Override
    public void visit(ParenthesesExprNode node) {}

    @Override
    public void visit(PreUnaryExprNode node) {}

    @Override
    public void visit(UnaryExprNode node) {}

    @Override
    public void visit(ArrayLiteralNode node) {}

    @Override
    public void visit(BoolLiteralNode node) {}

    @Override
    public void visit(FStringNode node) {}

    @Override
    public void visit(IdentifierNode node) {}

    @Override
    public void visit(IntLiteralNode node) {}

    @Override
    public void visit(NullLiteralNode node) {}

    @Override
    public void visit(StringLiteralNode node) {}

    @Override
    public void visit(ThisNode node) {}
}
