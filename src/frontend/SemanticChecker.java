package frontend;

import AST.*;
import AST.module.*;
import AST.expr.*;
import AST.expr.atom.*;
import AST.stmt.*;
import util.error.SemanticError;
import util.scope.*;
import util.type.*;

public class SemanticChecker implements ASTVisitor {
    private final GlobalScope gScope_;
    private Scope scope_ = null; // current scope

    public SemanticChecker(GlobalScope gScope) {
        gScope_ = gScope;
    }

    @Override
    public void visit(ProgramNode node) {
        scope_ = gScope_;
        FuncType mainFuncType = gScope_.getFuncType("main");
        if (mainFuncType == null) {
            throw new SemanticError("Main Function Missing Error", node.pos_);
        }
        if (!mainFuncType.returnType_.equals(new Type("int"))) {
            System.out.println("Invalid Type");
            throw new SemanticError("Main Function Return Type Error", node.pos_);
        }
        if (!mainFuncType.argTypeList_.isEmpty()) {
            System.out.println("Invalid Type");
            throw new SemanticError("Main Function Arg Error", node.pos_);
        }
        for (var def : node.defList_) {
            def.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(ClassDefNode node) {
        scope_ = node.scope_;
        for (var funcDef : node.funcDefList_) {
            funcDef.accept(this);
        }
        if (node.constructor_ != null) {
            if (!node.constructor_.name_.equals(node.name_)) {
                System.out.println("Undefined Identifier");
                throw new SemanticError("Constructor Name Error", node.pos_);
            }
            node.constructor_.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(ConstructorDefNode node) {
        scope_ = new FuncScope(scope_, new Type("void"));
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(FuncDefNode node) {
        scope_ = new FuncScope(scope_, new Type(node.returnType_));
        if (!node.returnType_.equals(new Type("void")) && gScope_.getClassType(node.returnType_.name_) == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Symbol Error", node.pos_);
        }
        for (var param : node.paramList_) {
            if (gScope_.getClassType(param.first_.name_) == null) {
                System.out.println("Undefined Identifier");
                throw new SemanticError("Undefined Symbol Error", node.pos_);
            }
            scope_.addVar(param.second_, param.first_, node.pos_);
        }
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        if (node.returnType_.equals(new Type("void"))) {
            if (node.stmtList_.isEmpty() ||
                !(node.stmtList_.get(node.stmtList_.size() - 1) instanceof ReturnStmtNode)) {
                node.stmtList_.add(new ReturnStmtNode(null, null));
                ((FuncScope) scope_).isReturned_ = true;
            }
        }
        if (node.name_.equals("main")) {
            if (node.stmtList_.isEmpty() ||
                !(node.stmtList_.get(node.stmtList_.size() - 1) instanceof ReturnStmtNode)) {
                node.stmtList_.add(new ReturnStmtNode(null, new IntLiteralNode(null, 0)));
                ((FuncScope) scope_).isReturned_ = true;
            }
        }
        if (!((FuncScope) scope_).isReturned_) {
            System.out.println("Missing Return Statement");
            throw new SemanticError("Missing Return Statement Error", node.pos_);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(VarDefNode node) {
        if (gScope_.getClassType(node.type_.name_) == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Symbol Error", node.pos_);
        }
        for (var pair : node.varList_) {
            if (pair.second_ != null) {
                pair.second_.accept(this);
                if (pair.second_ instanceof ArrayLiteralNode) {
                    if (!((ArrayLiteralNode) pair.second_).equalsType(node.type_)) {
                        System.out.println("Type Mismatch");
                        throw new SemanticError("Type Mismatch Error", node.pos_);
                    }
                    pair.second_ = new NewArrayExprNode(pair.second_.pos_, node.type_, (ArrayLiteralNode) pair.second_);
                }
                else {
                    if (!pair.second_.type_.equals(node.type_)) {
                        System.out.println("Type Mismatch");
                        throw new SemanticError("Type Mismatch Error", node.pos_);
                    }
                }
            }
            scope_.addVar(pair.first_, node.type_, node.pos_);
        }
    }

    @Override
    public void visit(BreakStmtNode node) {
        if (!scope_.isInLoop()) {
            System.out.println("Invalid Control Flow");
            throw new SemanticError("Break Statement Error", node.pos_);
        }
    }

    @Override
    public void visit(ContinueStmtNode node) {
        if (!scope_.isInLoop()) {
            System.out.println("Invalid Control Flow");
            throw new SemanticError("Continue Statement Error", node.pos_);
        }
    }

    @Override
    public void visit(ExprStmtNode node) {
        node.expr_.accept(this);
    }

    @Override
    public void visit(ForStmtNode node) {
        scope_ = new Scope(scope_);
        if (node.init_ != null) {
            node.init_.accept(this);
        }
        if (node.cond_ != null) {
            node.cond_.accept(this);
            if (!node.cond_.type_.equals(new Type("bool"))) {
                System.out.println("Invalid Type");
                throw new SemanticError("Type Mismatch Error: the type of cond should be bool", node.pos_);
            }
        }
        scope_ = new LoopScope(scope_);
        for (var stmt : node.body_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
        if (node.step_ != null) {
            node.step_.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(IfStmtNode node) {
        node.cond_.accept(this);
        if (!node.cond_.type_.equals(new Type("bool"))) {
            System.out.println("Invalid Type");
            throw new SemanticError("Type Mismatch Error: the type of cond should be bool", node.pos_);
        }
        scope_ = new Scope(scope_);
        for (var stmt : node.then_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
        scope_ = new Scope(scope_);
        for (var stmt : node.else_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(ReturnStmtNode node) {
        if (node.expr_ != null) {
            node.expr_.accept(this);
        }
        Type returnType = scope_.getReturnType();
        if (node.expr_ instanceof ArrayLiteralNode) {
            if (!((ArrayLiteralNode) node.expr_).equalsType(returnType)) {
                System.out.println("Type Mismatch");
                throw new SemanticError("Return Type Mismatch Error", node.pos_);
            }
        }
        else {
            if (!returnType.equals(node.expr_ == null ? new Type("void") : node.expr_.type_)) {
                System.out.println("Type Mismatch");
                throw new SemanticError("Return Type Mismatch Error", node.pos_);
            }
        }
    }

    @Override
    public void visit(SuiteStmtNode node) {
        scope_ = new Scope(scope_);
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(VarDefStmtNode node) {
        node.varDef_.accept(this);
    }

    @Override
    public void visit(WhileStmtNode node) {
        scope_ = new LoopScope(scope_);
        node.cond_.accept(this);
        if (!node.cond_.type_.equals(new Type("bool"))) {
            System.out.println("Invalid Type");
            throw new SemanticError("Type Mismatch Error: the type of cond should be bool", node.pos_);
        }
        scope_ = new Scope(scope_);
        for (var stmt : node.body_) {
            stmt.accept(this);
        }
        scope_ = scope_.parent_;
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(ArrayExprNode node) {
        node.array_.accept(this);
        node.index_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(AssignExprNode node) {
        node.lhs_.accept(this);
        node.rhs_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(BinaryExprNode node) {
        node.lhs_.accept(this);
        node.rhs_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(ConditionalExprNode node) {
        node.cond_.accept(this);
        node.lhs_.accept(this);
        node.rhs_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(FuncCallExprNode node) {
        if (node.funcName_ instanceof MemberExprNode) {
            node.funcName_.accept(this);
        }
        for (var arg : node.args_) {
            arg.accept(this);
        }
        FuncType funcType = null;
        if (node.funcName_ instanceof IdentifierNode) {
            funcType = scope_.getFuncType(((IdentifierNode) node.funcName_).name_);
        }
        else if (node.funcName_ instanceof MemberExprNode) {
            funcType = ((MemberExprNode) node.funcName_).funcType_;
        }
        if (funcType == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Function Error", node.pos_);
        }
        if (funcType.argTypeList_.size() != node.args_.size()) {
            System.out.println("Invalid Identifier");
            throw new SemanticError("Args Mismatch Error", node.pos_);
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (node.args_.get(i) instanceof ArrayLiteralNode) {
                if (!((ArrayLiteralNode) node.args_.get(i)).equalsType(funcType.argTypeList_.get(i))) {
                    System.out.println("Type Mismatch");
                    throw new SemanticError("Type Mismatch Error", node.pos_);
                }
            }
            else {
                if (!funcType.argTypeList_.get(i).equals(node.args_.get(i).type_)) {
                    System.out.println("Type Mismatch");
                    throw new SemanticError("Type Mismatch Error", node.pos_);
                }
            }
        }
        node.type_ = new Type(funcType.returnType_);
    }

    @Override
    public void visit(MemberExprNode node) {
        node.class_.accept(this);
        if (node.class_ instanceof ArrayLiteralNode) {
            if (!node.member_.equals("size")) {
                System.out.println("Undefined Identifier");
                throw new SemanticError("Undefined Symbol Error", node.pos_);
            }
            node.funcType_ = new FuncType(new Type("int"));
            node.isLeftValue_ = false;
            return;
        }
        if (node.class_.type_.isArray_) {
            if (!node.member_.equals("size")) {
                System.out.println("Undefined Identifier");
                throw new SemanticError("Undefined Symbol Error", node.pos_);
            }
            node.funcType_ = new FuncType(new Type("int"));
            node.isLeftValue_ = false;
        }
        else {
            ClassType classType = gScope_.getClassType(node.class_.type_.name_);
            FuncType funcType = classType.funcMap_.get(node.member_);
            Type varType = classType.varMap_.get(node.member_);
            if (funcType != null) {
                node.funcType_ = funcType;
                node.isLeftValue_ = false;
                return;
            }
            if (varType != null) {
                node.type_ = varType;
                return;
            }
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Symbol Error", node.pos_);
        }
    }

    @Override
    public void visit(NewArrayExprNode node) {
        for (var sz : node.fixedSizeList_) {
            sz.accept(this);
        }
        if (gScope_.getClassType(node.type_.name_) == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Class Error", node.pos_);
        }
        node.checkAndInferType();
    }

    @Override
    public void visit(NewVarExprNode node) {
        if (gScope_.getClassType(node.type_.name_) == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Class Error", node.pos_);
        }
    }

    @Override
    public void visit(ParenthesesExprNode node) {
        node.expr_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(PreUnaryExprNode node) {
        node.expr_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(UnaryExprNode node) {
        node.expr_.accept(this);
        node.checkAndInferType();
    }

    @Override
    public void visit(ArrayLiteralNode node) {
        node.checkAndInferType();
    }

    @Override
    public void visit(BoolLiteralNode node) {}

    @Override
    public void visit(FStringNode node) {
        for (var expr : node.exprList_) {
            expr.accept(this);
        }
        node.checkAndInferType();
    }

    @Override
    public void visit(IdentifierNode node) {
        Type varType = scope_.getVarType(node.name_);
        if (varType == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("Undefined Variable Error", node.pos_);
        }
        node.type_ = varType;
    }

    @Override
    public void visit(IntLiteralNode node) {}

    @Override
    public void visit(NullLiteralNode node) {}

    @Override
    public void visit(StringLiteralNode node) {}

    @Override
    public void visit(ThisNode node) {
        String className = scope_.getClassName();
        if (className == null) {
            System.out.println("Undefined Identifier");
            throw new SemanticError("This Expression Error", node.pos_);
        }
        node.type_ = new Type(className);
    }
}
