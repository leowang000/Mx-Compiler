package frontend;

import java.util.ArrayList;

import AST.ASTVisitor;
import AST.module.*;
import AST.expr.*;
import AST.expr.atom.*;
import AST.stmt.*;
import IR.inst.*;
import IR.module.*;
import IR.type.*;
import IR.value.IRValue;
import IR.value.constant.*;
import IR.value.var.*;
import util.scope.*;
import util.type.Type;

public class IRBuilder implements ASTVisitor {
    private Scope scope_;
    private IRProgram irProgram_;
    private IRBasicBlock currentBlock_, initBlock_;

    public IRBuilder(GlobalScope gScope, IRProgram irProgram) {
        scope_ = gScope;
        irProgram_ = irProgram;
        currentBlock_ = null;
        initBlock_ = null;
    }

    @Override
    public void visit(ProgramNode node) {
        IRFuncDef initFunc = new IRFuncDef("builtin.init", new IRVoidType());
        initBlock_ = new IRBasicBlock("builtin.init_entry", initFunc);
        for (var def : node.defList_) {
            def.accept(this);
        }
        initFunc.body_.add(initBlock_);
        irProgram_.funcDefMap_.put("builtin.init", initFunc);
    }

    @Override
    public void visit(ClassDefNode node) {
        scope_ = new ClassScope(scope_, node.name_);
        IRStructDef irStructDef = new IRStructDef(node.name_);
        ArrayList<IRType> fields = new ArrayList<>();
        for (var varDef : node.varDefList_) {
            IRType irType = IRType.toIRType(varDef.type_);
            for (var pair : varDef.varList_) {
                fields.add(irType);
                irStructDef.struct_.varToIdMap_.put(pair.first_, fields.size() - 1);
            }
        }
        irStructDef.struct_.setFields(fields);
        irProgram_.structList_.put(node.name_, irStructDef);
        if (node.constructor_ != null) {
            node.constructor_.accept(this);
        }
        for (var funcDef : node.funcDefList_) {
            funcDef.accept(this);
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(ConstructorDefNode node) {
        scope_ = new FuncScope(scope_, new Type("void"));
        IRType returnType = new IRVoidType();
        String funcName = String.format("struct.%s.%s", node.name_, node.name_);
        IRFuncDecl funcDecl = new IRFuncDecl(funcName, returnType);
        IRFuncDef funcDef = new IRFuncDef(funcName, returnType);
        currentBlock_ = new IRBasicBlock(funcName + "_entry", funcDef);
        IRType type = new IRPtrType(new IRStructType(node.name_));
        IRLocalVar localVar = IRLocalVar.newLocalVar(type);
        funcDecl.argTypeList_.add(type);
        funcDef.args_.add(localVar);
        addVarAndAssign("this", localVar);
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        funcDef.body_.add(currentBlock_);
        irProgram_.funcDeclList_.add(funcDecl);
        irProgram_.funcDefMap_.put(funcName, funcDef);
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(FuncDefNode node) {
        scope_ = new FuncScope(scope_, node.returnType_);
        boolean isMemberFunction = (scope_.parent_ instanceof ClassScope);
        IRType returnType = IRType.toIRType(node.returnType_);
        String funcName =
                (isMemberFunction ? String.format("struct.%s.%s", scope_.getClassName(), node.name_) : node.name_);
        IRFuncDecl funcDecl = new IRFuncDecl(funcName, returnType);
        IRFuncDef funcDef = new IRFuncDef(funcName, returnType);
        currentBlock_ = new IRBasicBlock(funcName + "_entry", funcDef);
        if (isMemberFunction) {
            IRType type = new IRPtrType(new IRStructType(scope_.getClassName()));
            IRLocalVar localVar = new IRLocalVar("this", type);
            funcDecl.argTypeList_.add(type);
            funcDef.args_.add(localVar);
        }
        for (var pair : node.paramList_) {
            IRType type = IRType.toIRType(pair.first_);
            IRLocalVar localVar = IRLocalVar.newLocalVar(type);
            funcDecl.argTypeList_.add(type);
            funcDef.args_.add(localVar);
            addVarAndAssign(pair.second_, localVar);
        }
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        funcDef.body_.add(currentBlock_);
        irProgram_.funcDeclList_.add(funcDecl);
        irProgram_.funcDefMap_.put(funcName, funcDef);
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(VarDefNode node) {
        boolean isGlobalVar = (scope_ instanceof GlobalScope);
        IRType type = IRType.toIRType(node.type_);
        if (isGlobalVar) {
            for (var pair : node.varList_) {
                IRGlobalVar newVar = (IRGlobalVar) scope_.irAddVar(pair.first_, new IRPtrType(type));
                irProgram_.globalVarList_.add(new IRGlobalVarDef(newVar));
                if (pair.second_ == null) {
                    continue;
                }
                IRBasicBlock tmp = currentBlock_;
                currentBlock_ = initBlock_;
                pair.second_.accept(this);
                IRLocalVar value =
                        (pair.second_.isLeftValue_ ? dereferencePointer(currentBlock_.result_) : currentBlock_.result_);
                currentBlock_.instList_.add(new IRStoreInst(value, newVar));
                initBlock_ = currentBlock_;
                currentBlock_ = tmp;
            }
        }
        else {
            for (var pair : node.varList_) {
                IRLocalVar newVar = (IRLocalVar) scope_.irAddVar(pair.first_, new IRPtrType(type));
                currentBlock_.instList_.add(new IRAllocaInst(newVar));
                if (pair.second_ == null) {
                    continue;
                }
                if (pair.second_ instanceof NewArrayExprNode) {
                    ArrayLiteralNode array = ((NewArrayExprNode) pair.second_).array_;
                    if (array != null) {
                        array.accept(this);
                        IRLocalVar tmp = IRLocalVar.newLocalVar(type);
                        currentBlock_.instList_.add(new IRCallInst(tmp, "array.copy", currentBlock_.result_,
                                                                   new IRIntConst(((IRPtrType) type).base_.getSize()),
                                                                   new IRIntConst(((IRPtrType) type).dim_)));
                        currentBlock_.instList_.add(new IRStoreInst(tmp, newVar));
                        continue;
                    }
                }
                pair.second_.accept(this);
                IRLocalVar value =
                        (pair.second_.isLeftValue_ ? dereferencePointer(currentBlock_.result_) : currentBlock_.result_);
                currentBlock_.instList_.add(new IRStoreInst(value, newVar));
            }
        }
    }

    @Override
    public void visit(BreakStmtNode node) {

    }

    @Override
    public void visit(ContinueStmtNode node) {

    }

    @Override
    public void visit(ExprStmtNode node) {

    }

    @Override
    public void visit(ForStmtNode node) {

    }

    @Override
    public void visit(IfStmtNode node) {

    }

    @Override
    public void visit(ReturnStmtNode node) {

    }

    @Override
    public void visit(SuiteStmtNode node) {

    }

    @Override
    public void visit(VarDefStmtNode node) {

    }

    @Override
    public void visit(WhileStmtNode node) {

    }

    @Override
    public void visit(ArrayExprNode node) {

    }

    @Override
    public void visit(AssignExprNode node) {

    }

    @Override
    public void visit(BinaryExprNode node) {

    }

    @Override
    public void visit(ConditionalExprNode node) {

    }

    @Override
    public void visit(FuncCallExprNode node) {

    }

    @Override
    public void visit(MemberExprNode node) {

    }

    @Override
    public void visit(NewArrayExprNode node) {

    }

    @Override
    public void visit(NewVarExprNode node) {

    }

    @Override
    public void visit(ParenthesesExprNode node) {
        node.expr_.accept(this);
    }

    @Override
    public void visit(PreUnaryExprNode node) {

    }

    @Override
    public void visit(UnaryExprNode node) {

    }

    @Override
    public void visit(ArrayLiteralNode node) {

    }

    @Override
    public void visit(BoolLiteralNode node) {

    }

    @Override
    public void visit(FStringNode node) {

    }

    @Override
    public void visit(IdentifierNode node) {

    }

    @Override
    public void visit(IntLiteralNode node) {

    }

    @Override
    public void visit(NullLiteralNode node) {

    }

    @Override
    public void visit(StringLiteralNode node) {

    }

    @Override
    public void visit(ThisNode node) {

    }

    private void addVarAndAssign(String varName, IRLocalVar value) {
        IRLocalVar newVar = (IRLocalVar) scope_.irAddVar(varName, new IRPtrType(value.type_));
        currentBlock_.instList_.add(new IRAllocaInst(newVar));
        currentBlock_.instList_.add(new IRStoreInst(value, newVar));
    }

    private IRLocalVar dereferencePointer(IRLocalVar var) {
        IRLocalVar newVar = IRLocalVar.newLocalVar(((IRPtrType) var.type_).getDereferenceType());
        currentBlock_.instList_.add(new IRLoadInst(newVar, var));
        return newVar;
    }
}
