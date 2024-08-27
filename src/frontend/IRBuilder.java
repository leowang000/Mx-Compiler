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
    private final GlobalScope gScope_;
    private final IRProgram irProgram_;
    private IRBasicBlock currentBlock_ = null, initBlock_ = null, loopEnd_ = null, loopNext_ = null;
    private boolean endBlock_ = false, visitMemberFunction_ = false;
    private int forCnt_ = 0, whileCnt_ = 0, ifCnt_ = 0, ternaryCnt_ = 0, andCnt_ = 0, orCnt_ = 0, stringCnt_ = 0,
            fStringCnt_ = 0, arrayCnt_ = 0, newCnt_ = 0;

    public IRBuilder(GlobalScope gScope, IRProgram irProgram) {
        scope_ = gScope;
        gScope_ = gScope;
        irProgram_ = irProgram;
    }

    @Override
    public void visit(ProgramNode node) {
        IRFuncDef initFunc = new IRFuncDef("builtin.init", new IRVoidType());
        initBlock_ = new IRBasicBlock("builtin.init_entry", initFunc);
        for (var def : node.defList_) {
            if (def instanceof ClassDefNode) {
                def.accept(this);
            }
        }
        for (var def : node.defList_) {
            if (def instanceof VarDefNode) {
                def.accept(this);
            }
        }
        visitMemberFunction_ = true;
        for (var def : node.defList_) {
            if (def instanceof ClassDefNode) {
                def.accept(this);
            }
        }
        for (var def : node.defList_) {
            if (def instanceof FuncDefNode) {
                def.accept(this);
            }
        }
        initBlock_.instList_.add(new IRRetInst(null));
        initFunc.body_.add(initBlock_);
        irProgram_.funcDefMap_.put("builtin.init", initFunc);
    }

    @Override
    public void visit(ClassDefNode node) {
        if (!visitMemberFunction_) {
            IRStructDef irStructDef = new IRStructDef(node.name_, node.constructor_ != null);
            ArrayList<IRType> fields = new ArrayList<>();
            for (var varDef : node.varDefList_) {
                IRType irType = IRType.toIRType(varDef.type_);
                for (var pair : varDef.varList_) {
                    fields.add(irType);
                    irStructDef.struct_.varToIdMap_.put(pair.first_, fields.size() - 1);
                }
            }
            irStructDef.struct_.setFields(fields);
            if (node.constructor_ != null) {
                irStructDef.memberFuncSet_.add(node.constructor_.name_);
            }
            for (var funcDef : node.funcDefList_) {
                irStructDef.memberFuncSet_.add(funcDef.name_);
            }
            irProgram_.structDefMap_.put(node.name_, irStructDef);
            return;
        }
        scope_ = new ClassScope(scope_, node.name_);
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
        IRFuncDef funcDef = new IRFuncDef(funcName, returnType);
        currentBlock_ = new IRBasicBlock(funcName + "_entry", funcDef);
        IRType type = new IRPtrType(new IRStructType(node.name_));
        IRLocalVar localVar = new IRLocalVar("this", type);
        funcDef.args_.add(localVar);
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
        }
        irProgram_.funcDefMap_.put(funcName, funcDef);
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(FuncDefNode node) {
        scope_ = new FuncScope(scope_, node.returnType_);
        boolean isMemberFunction = !(scope_.parent_ == gScope_);
        IRType returnType = IRType.toIRType(node.returnType_);
        String funcName =
                (isMemberFunction ? String.format("struct.%s.%s", scope_.getClassName(), node.name_) : node.name_);
        IRFuncDef funcDef = new IRFuncDef(funcName, returnType);
        currentBlock_ = new IRBasicBlock(funcName + "_entry", funcDef);
        if (node.name_.equals("main")) {
            currentBlock_.instList_.add(new IRCallInst(null, "builtin.init"));
        }
        if (isMemberFunction) {
            IRType type = new IRPtrType(new IRStructType(scope_.getClassName()));
            IRLocalVar localVar = new IRLocalVar("this", type);
            funcDef.args_.add(localVar);
        }
        for (var pair : node.paramList_) {
            IRType type = IRType.toIRType(pair.first_);
            IRLocalVar localVar = IRLocalVar.newLocalVar(type);
            funcDef.args_.add(localVar);
            addVarAndAssign(pair.second_, localVar);
        }
        endBlock_ = false;
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
            if (endBlock_) {
                break;
            }
        }
        endBlock_ = false;
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
                IRBasicBlock oldCurrent = currentBlock_;
                currentBlock_ = initBlock_;
                pair.second_.accept(this);
                IRValue value = getValueResult(pair.second_.isLeftValue_);
                currentBlock_.instList_.add(new IRStoreInst(value, newVar));
                initBlock_ = currentBlock_;
                currentBlock_ = oldCurrent;
            }
        }
        else {
            for (var pair : node.varList_) {
                IRLocalVar newVar = (IRLocalVar) scope_.irAddVar(pair.first_, new IRPtrType(type));
                currentBlock_.instList_.add(new IRAllocaInst(newVar));
                if (pair.second_ == null) {
                    continue;
                }
                pair.second_.accept(this);
                IRValue value = getValueResult(pair.second_.isLeftValue_);
                currentBlock_.instList_.add(new IRStoreInst(value, newVar));
            }
        }
    }

    @Override
    public void visit(BreakStmtNode node) {
        currentBlock_.instList_.add(new IRJumpInst(loopEnd_));
        submitBlock();
    }

    @Override
    public void visit(ContinueStmtNode node) {
        currentBlock_.instList_.add(new IRJumpInst(loopNext_));
        submitBlock();
    }

    @Override
    public void visit(ExprStmtNode node) {
        node.expr_.accept(this);
        endBlock_ = false;
    }

    @Override
    public void visit(ForStmtNode node) {
        scope_ = new Scope(scope_);
        IRBasicBlock oldLoopNext = loopNext_, oldLoopEnd = loopEnd_;
        int id = forCnt_++;
        IRBasicBlock cond = new IRBasicBlock("for_cond_" + id, currentBlock_.belong_);
        IRBasicBlock body = new IRBasicBlock("for_body_" + id, currentBlock_.belong_);
        IRBasicBlock step = new IRBasicBlock("for_step_" + id, currentBlock_.belong_);
        IRBasicBlock end = new IRBasicBlock("for_end_" + id, currentBlock_.belong_);
        loopNext_ = step;
        loopEnd_ = end;
        if (node.init_ != null) {
            node.init_.accept(this);
        }
        currentBlock_.instList_.add(new IRJumpInst(cond));
        submitBlock();
        currentBlock_ = cond;
        node.cond_.accept(this);
        IRValue value = getValueResult(node.cond_.isLeftValue_);
        currentBlock_.instList_.add(new IRBrInst(value, body, end));
        submitBlock();
        currentBlock_ = body;
        scope_ = new LoopScope(scope_);
        endBlock_ = false;
        for (var stmt : node.body_) {
            stmt.accept(this);
            if (endBlock_) {
                break;
            }
        }
        if (!endBlock_) {
            currentBlock_.instList_.add(new IRJumpInst(step));
            submitBlock();
        }
        endBlock_ = false;
        scope_ = scope_.parent_;
        currentBlock_ = step;
        if (node.step_ != null) {
            node.step_.accept(this);
        }
        currentBlock_.instList_.add(new IRJumpInst(cond));
        submitBlock();
        currentBlock_ = end;
        loopNext_ = oldLoopNext;
        loopEnd_ = oldLoopEnd;
        scope_ = scope_.parent_;
        endBlock_ = false;
    }

    @Override
    public void visit(IfStmtNode node) {
        int id = ifCnt_++;
        IRBasicBlock then = new IRBasicBlock("if_then_" + id, currentBlock_.belong_);
        IRBasicBlock els = new IRBasicBlock("if_else_" + id, currentBlock_.belong_);
        IRBasicBlock end = new IRBasicBlock("if_end_" + id, currentBlock_.belong_);
        node.cond_.accept(this);
        IRValue value = getValueResult(node.cond_.isLeftValue_);
        currentBlock_.instList_.add(new IRBrInst(value, then, node.else_.isEmpty() ? end : els));
        submitBlock();
        currentBlock_ = then;
        scope_ = new Scope(scope_);
        endBlock_ = false;
        for (var stmt : node.then_) {
            stmt.accept(this);
            if (endBlock_) {
                break;
            }
        }
        if (!endBlock_) {
            currentBlock_.instList_.add(new IRJumpInst(end));
            submitBlock();
        }
        endBlock_ = false;
        scope_ = scope_.parent_;
        if (!node.else_.isEmpty()) {
            currentBlock_ = els;
            scope_ = new Scope(scope_);
            endBlock_ = false;
            for (var stmt : node.else_) {
                stmt.accept(this);
                if (endBlock_) {
                    break;
                }
            }
            if (!endBlock_) {
                currentBlock_.instList_.add(new IRJumpInst(end));
                submitBlock();
            }
            endBlock_ = false;
            scope_ = scope_.parent_;
        }
        currentBlock_ = end;
    }

    @Override
    public void visit(ReturnStmtNode node) {
        if (node.expr_ == null) {
            currentBlock_.instList_.add(new IRRetInst(null));
            submitBlock();
            return;
        }
        node.expr_.accept(this);
        if (currentBlock_.result_ == null) {
            currentBlock_.instList_.add(new IRRetInst(null));
            submitBlock();
            return;
        }
        IRValue value = getValueResult(node.expr_.isLeftValue_);
        currentBlock_.instList_.add(new IRRetInst(value));
        submitBlock();
    }

    @Override
    public void visit(SuiteStmtNode node) {
        scope_ = new Scope(scope_);
        for (var stmt : node.stmtList_) {
            stmt.accept(this);
            if (endBlock_) {
                break;
            }
        }
        scope_ = scope_.parent_;
    }

    @Override
    public void visit(VarDefStmtNode node) {
        node.varDef_.accept(this);
        endBlock_ = false;
    }

    @Override
    public void visit(WhileStmtNode node) {
        IRBasicBlock oldLoopNext = loopNext_, oldLoopEnd = loopEnd_;
        int id = whileCnt_++;
        IRBasicBlock cond = new IRBasicBlock("while_cond_" + id, currentBlock_.belong_);
        IRBasicBlock body = new IRBasicBlock("while_body_" + id, currentBlock_.belong_);
        IRBasicBlock end = new IRBasicBlock("while_end_" + id, currentBlock_.belong_);
        loopNext_ = cond;
        loopEnd_ = end;
        currentBlock_.instList_.add(new IRJumpInst(cond));
        submitBlock();
        currentBlock_ = cond;
        node.cond_.accept(this);
        IRValue value = getValueResult(node.cond_.isLeftValue_);
        currentBlock_.instList_.add(new IRBrInst(value, body, end));
        submitBlock();
        currentBlock_ = body;
        scope_ = new LoopScope(scope_);
        endBlock_ = false;
        for (var stmt : node.body_) {
            stmt.accept(this);
            if (endBlock_) {
                break;
            }
        }
        if (!endBlock_) {
            currentBlock_.instList_.add(new IRJumpInst(cond));
            submitBlock();
        }
        endBlock_ = false;
        scope_ = scope_.parent_;
        currentBlock_ = end;
        loopNext_ = oldLoopNext;
        loopEnd_ = oldLoopEnd;
    }

    @Override
    public void visit(ArrayExprNode node) {
        node.array_.accept(this);
        IRValue array = getValueResult(node.array_.isLeftValue_);
        node.index_.accept(this);
        IRValue index = getValueResult(node.index_.isLeftValue_);
        IRLocalVar newVar = IRLocalVar.newLocalVar(array.type_);
        currentBlock_.instList_.add(new IRGetElementPtrInst(newVar, array, index));
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(AssignExprNode node) {
        node.lhs_.accept(this);
        IRValue lhs = currentBlock_.result_;
        node.rhs_.accept(this);
        IRValue rhs = getValueResult(node.rhs_.isLeftValue_);
        currentBlock_.instList_.add(new IRStoreInst(rhs, lhs));
        currentBlock_.result_ = null;
    }

    @Override
    public void visit(BinaryExprNode node) {
        if (node.op_.equals("&&")) {
            int id = andCnt_++;
            IRBasicBlock rhs = new IRBasicBlock("and_rhs_" + id, currentBlock_.belong_);
            IRBasicBlock end = new IRBasicBlock("and_end_" + id, currentBlock_.belong_);
            node.lhs_.accept(this);
            IRValue lhsValue = getValueResult(node.lhs_.isLeftValue_);
            IRLocalVar resultPtr = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(1)));
            currentBlock_.instList_.add(new IRAllocaInst(resultPtr));
            currentBlock_.instList_.add(new IRStoreInst(new IRBoolConst(false), resultPtr));
            currentBlock_.instList_.add(new IRBrInst(lhsValue, rhs, end));
            submitBlock();
            currentBlock_ = rhs;
            node.rhs_.accept(this);
            IRValue rhsValue = getValueResult(node.rhs_.isLeftValue_);
            currentBlock_.instList_.add(new IRStoreInst(rhsValue, resultPtr));
            currentBlock_.instList_.add(new IRJumpInst(end));
            submitBlock();
            currentBlock_ = end;
            IRLocalVar newVar = IRLocalVar.newLocalVar(new IRIntType(1));
            currentBlock_.instList_.add(new IRLoadInst(newVar, resultPtr));
            currentBlock_.result_ = newVar;
            return;
        }
        if (node.op_.equals("||")) {
            int id = orCnt_++;
            IRBasicBlock rhs = new IRBasicBlock("or_rhs_" + id, currentBlock_.belong_);
            IRBasicBlock end = new IRBasicBlock("or_end_" + id, currentBlock_.belong_);
            node.lhs_.accept(this);
            IRValue lhsValue = getValueResult(node.lhs_.isLeftValue_);
            IRLocalVar resultPtr = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(1)));
            currentBlock_.instList_.add(new IRAllocaInst(resultPtr));
            currentBlock_.instList_.add(new IRStoreInst(new IRBoolConst(true), resultPtr));
            currentBlock_.instList_.add(new IRBrInst(lhsValue, end, rhs));
            submitBlock();
            currentBlock_ = rhs;
            node.rhs_.accept(this);
            IRValue rhsValue = getValueResult(node.rhs_.isLeftValue_);
            currentBlock_.instList_.add(new IRStoreInst(rhsValue, resultPtr));
            currentBlock_.instList_.add(new IRJumpInst(end));
            submitBlock();
            currentBlock_ = end;
            IRLocalVar newVar = IRLocalVar.newLocalVar(new IRIntType(1));
            currentBlock_.instList_.add(new IRLoadInst(newVar, resultPtr));
            currentBlock_.result_ = newVar;
            return;
        }
        IRLocalVar newVar = null;
        node.lhs_.accept(this);
        IRValue lhsValue = getValueResult(node.lhs_.isLeftValue_);
        node.rhs_.accept(this);
        IRValue rhsValue = getValueResult(node.rhs_.isLeftValue_);
        switch (node.op_) {
            case "+":
                if (lhsValue.type_ instanceof IRIntType) {
                    newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                    currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "add"));
                }
                else {
                    newVar = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(32)));
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_add", lhsValue, rhsValue));
                }
                break;
            case "-":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "sub"));
                break;
            case "*":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "mul"));
                break;
            case "/":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "sdiv"));
                break;
            case "%":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "srem"));
                break;
            case "&":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "and"));
                break;
            case "|":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "or"));
                break;
            case "^":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "xor"));
                break;
            case "<<":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "shl"));
                break;
            case ">>":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, lhsValue, rhsValue, "ashr"));
                break;
            case "<":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_ instanceof IRIntType) {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "slt"));
                }
                else {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_le", lhsValue, rhsValue));
                }
                break;
            case ">":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_ instanceof IRIntType) {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "sgt"));
                }
                else {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_ge", lhsValue, rhsValue));
                }
                break;
            case "<=":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_ instanceof IRIntType) {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "sle"));
                }
                else {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_leq", lhsValue, rhsValue));
                }
                break;
            case ">=":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_ instanceof IRIntType) {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "sge"));
                }
                else {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_geq", lhsValue, rhsValue));
                }
                break;
            case "==":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_.equals(new IRPtrType(new IRIntType(8)))) {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_eq", lhsValue, rhsValue));
                }
                else {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "eq"));
                }
                break;
            case "!=":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                if (lhsValue.type_.equals(new IRPtrType(new IRIntType(8)))) {
                    currentBlock_.instList_.add(new IRCallInst(newVar, "builtin.string_ne", lhsValue, rhsValue));
                }
                else {
                    currentBlock_.instList_.add(new IRIcmpInst(newVar, lhsValue, rhsValue, "ne"));
                }
                break;
        }
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(FuncCallExprNode node) {
        IRCallInst callInst;
        if (node.funcName_ instanceof MemberExprNode) {
            MemberExprNode funcNameNode = (MemberExprNode) node.funcName_;
            funcNameNode.class_.accept(this);
            IRValue clas = getValueResult(funcNameNode.class_.isLeftValue_);
            String funcName;
            if (funcNameNode.class_.type_.isArray_) {
                funcName = "array." + funcNameNode.member_;
            }
            else if (funcNameNode.class_.type_.equals(new Type("string"))) {
                funcName = "string." + funcNameNode.member_;
            }
            else {
                funcName = String.format("struct.%s.%s", funcNameNode.class_.type_.name_, funcNameNode.member_);
            }
            callInst = new IRCallInst(null, funcName);
            callInst.args_.add(clas);
        }
        else {
            String funcName = ((IdentifierNode) node.funcName_).name_;
            IRStructDef currentStructDef = getCurrentStructDef();
            if (currentStructDef != null && currentStructDef.memberFuncSet_.contains(funcName)) {
                callInst = new IRCallInst(null, String.format("struct.%s.%s", scope_.getClassName(), funcName));
                callInst.args_.add(currentBlock_.belong_.args_.get(0));
            }
            else {
                callInst = new IRCallInst(null, funcName);
            }
        }
        for (var arg : node.args_) {
            arg.accept(this);
            IRValue value = getValueResult(arg.isLeftValue_);
            callInst.args_.add(value);
        }
        IRLocalVar newVar =
                (node.type_.equals(new Type("void")) ? null : IRLocalVar.newLocalVar(IRType.toIRType(node.type_)));
        callInst.result_ = newVar;
        currentBlock_.instList_.add(callInst);
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(MemberExprNode node) {
        node.class_.accept(this);
        IRValue clas = getValueResult(node.class_.isLeftValue_);
        int id = irProgram_.structDefMap_.get(
                ((IRStructType) ((IRPtrType) clas.type_).base_).name_).struct_.varToIdMap_.get(node.member_);
        IRLocalVar newVar = IRLocalVar.newLocalVar(new IRPtrType(IRType.toIRType(node.type_)));
        currentBlock_.instList_.add(new IRGetElementPtrInst(newVar, clas, id));
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(NewArrayExprNode node) {
        IRType type = IRType.toIRType(node.type_);
        if (node.array_ == null) {
            ArrayList<IRValue> fixedSizeList = new ArrayList<>();
            for (var sz : node.fixedSizeList_) {
                sz.accept(this);
                fixedSizeList.add(getValueResult(sz.isLeftValue_));
            }
            IRLocalVar res = generateArray(fixedSizeList, 0, (IRPtrType) type);
            currentBlock_.result_ = res;
            return;
        }
        node.array_.accept(this);
        IRLocalVar newVar = IRLocalVar.newLocalVar(type);
        currentBlock_.instList_.add(new IRCallInst(newVar, "array.copy", currentBlock_.result_, new IRIntConst(
                IRType.toIRType(new Type(node.type_.name_, 0)).getSize()), new IRIntConst(node.type_.dim_)));
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(NewVarExprNode node) {
        IRStructDef struct = irProgram_.structDefMap_.get(node.type_.name_);
        IRLocalVar newVar = IRLocalVar.newLocalVar(new IRPtrType(new IRStructType(node.type_.name_)));
        if (struct.hasConstructor_) {
            currentBlock_.instList_.add(
                    new IRCallInst(newVar, "builtin.malloc", new IRIntConst(struct.struct_.getSize())));
            currentBlock_.instList_.add(
                    new IRCallInst(null, String.format("struct.%s.%s", node.type_.name_, node.type_.name_), newVar));
        }
        else {
            currentBlock_.instList_.add(
                    new IRCallInst(newVar, "builtin.calloc", new IRIntConst(struct.struct_.getSize())));
        }
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(ParenthesesExprNode node) {
        node.expr_.accept(this);
    }

    @Override
    public void visit(PreUnaryExprNode node) {
        node.expr_.accept(this);
        IRLocalVar tmp1 = IRLocalVar.newLocalVar(new IRIntType(32));
        currentBlock_.instList_.add(new IRLoadInst(tmp1, currentBlock_.result_));
        IRLocalVar tmp2 = IRLocalVar.newLocalVar(new IRIntType(32));
        currentBlock_.instList_.add(
                new IRBinaryInst(tmp2, tmp1, new IRIntConst(node.op_.equals("++") ? 1 : -1), "add"));
        currentBlock_.instList_.add(new IRStoreInst(tmp2, currentBlock_.result_));
    }

    @Override
    public void visit(TernaryExprNode node) {
        int id = ternaryCnt_++;
        boolean isResultVoid = (node.lhs_.type_.equals(new Type("void")));
        IRBasicBlock lhs = new IRBasicBlock("ternary_lhs_" + id, currentBlock_.belong_);
        IRBasicBlock rhs = new IRBasicBlock("ternary_rhs_" + id, currentBlock_.belong_);
        IRBasicBlock end = new IRBasicBlock("ternary_end_" + id, currentBlock_.belong_);
        node.cond_.accept(this);
        IRValue cond = getValueResult(node.cond_.isLeftValue_);
        IRLocalVar resultPtr = null;
        if (!isResultVoid) {
            resultPtr = IRLocalVar.newLocalVar(new IRPtrType(IRType.toIRType(node.lhs_.type_)));
            currentBlock_.instList_.add(new IRAllocaInst(resultPtr));
        }
        currentBlock_.instList_.add(new IRBrInst(cond, lhs, rhs));
        submitBlock();
        currentBlock_ = lhs;
        node.lhs_.accept(this);
        IRValue lhsValue;
        if (!isResultVoid) {
            lhsValue = getValueResult(node.lhs_.isLeftValue_);
            currentBlock_.instList_.add(new IRStoreInst(lhsValue, resultPtr));
        }
        currentBlock_.instList_.add(new IRJumpInst(end));
        submitBlock();
        currentBlock_ = rhs;
        node.rhs_.accept(this);
        IRValue rhsValue;
        if (!isResultVoid) {
            rhsValue = getValueResult(node.rhs_.isLeftValue_);
            currentBlock_.instList_.add(new IRStoreInst(rhsValue, resultPtr));
        }
        currentBlock_.instList_.add(new IRJumpInst(end));
        submitBlock();
        currentBlock_ = end;
        if (!isResultVoid) {
            IRLocalVar newVar = IRLocalVar.newLocalVar(IRType.toIRType(node.lhs_.type_));
            currentBlock_.instList_.add(new IRLoadInst(newVar, resultPtr));
            currentBlock_.result_ = newVar;
        }
    }

    @Override
    public void visit(UnaryExprNode node) {
        node.expr_.accept(this);
        if (node.op_.equals("++") || node.op_.equals("--")) {
            IRLocalVar res = IRLocalVar.newLocalVar(new IRIntType(32));
            currentBlock_.instList_.add(new IRLoadInst(res, currentBlock_.result_));
            IRLocalVar tmp = IRLocalVar.newLocalVar(new IRIntType(32));
            currentBlock_.instList_.add(
                    new IRBinaryInst(tmp, res, new IRIntConst(node.op_.equals("++") ? 1 : -1), "add"));
            currentBlock_.instList_.add(new IRStoreInst(tmp, currentBlock_.result_));
            currentBlock_.result_ = res;
            return;
        }
        IRLocalVar newVar = null;
        IRValue expr = getValueResult(node.expr_.isLeftValue_);
        switch (node.op_) {
            case "+":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, new IRIntConst(0), expr, "add"));
                break;
            case "-":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, new IRIntConst(0), expr, "sub"));
                break;
            case "~":
                newVar = IRLocalVar.newLocalVar(new IRIntType(32));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, new IRIntConst(-1), expr, "xor"));
                break;
            case "!":
                newVar = IRLocalVar.newLocalVar(new IRIntType(1));
                currentBlock_.instList_.add(new IRBinaryInst(newVar, new IRBoolConst(true), expr, "xor"));
                break;
        }
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(ArrayLiteralNode node) {
        int id = arrayCnt_++;
        IRType type = IRType.toIRType(node.type_);
        IRGlobalVar arrayDef = new IRGlobalVar("array." + id, new IRPtrType(type));
        irProgram_.arrayLiteralList_.add(new IRGlobalVarDef(arrayDef));
        IRBasicBlock oldCurrent = currentBlock_;
        currentBlock_ = initBlock_;
        currentBlock_.instList_.add(new IRStoreInst(initArrayLiteral(node, (IRPtrType) type), arrayDef));
        currentBlock_ = oldCurrent;
        IRLocalVar newVar = IRLocalVar.newLocalVar(type);
        currentBlock_.instList_.add(new IRLoadInst(newVar, arrayDef));
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(BoolLiteralNode node) {
        currentBlock_.result_ = new IRBoolConst(node.value_);
    }

    @Override
    public void visit(FStringNode node) {
        int id = fStringCnt_++;
        ArrayList<IRGlobalVar> strList = new ArrayList<>();
        ArrayList<IRValue> exprList = new ArrayList<>();
        for (int i = 0; i < node.strList_.size(); i++) {
            IRGlobalVar fStringLiteral =
                    new IRGlobalVar(String.format("fString.%d.%d", id, i), new IRPtrType(new IRIntType(8)));
            irProgram_.fStringList_.add(new IRStringLiteralDef(fStringLiteral, node.strList_.get(i)));
            strList.add(fStringLiteral);
        }
        for (var expr : node.exprList_) {
            expr.accept(this);
            IRValue value = getValueResult(expr.isLeftValue_);
            if (expr.type_.equals(new Type("string"))) {
                exprList.add(value);
            }
            else if (expr.type_.equals(new Type("bool"))) {
                IRLocalVar boolValue = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(8)));
                currentBlock_.instList_.add(new IRCallInst(boolValue, "builtin.bool_to_string", value));
                exprList.add(boolValue);
            }
            else {
                IRLocalVar intValue = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(8)));
                currentBlock_.instList_.add(new IRCallInst(intValue, "toString", value));
                exprList.add(intValue);
            }
        }
        IRValue newVar = strList.get(0);
        for (int i = 0; i < exprList.size(); i++) {
            IRLocalVar tmp = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(8)));
            currentBlock_.instList_.add(new IRCallInst(tmp, "builtin.string_add", newVar, exprList.get(i)));
            newVar = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(8)));
            currentBlock_.instList_.add(
                    new IRCallInst((IRLocalVar) newVar, "builtin.string_add", tmp, strList.get(i + 1)));
        }
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(IdentifierNode node) {
        IRValue newVar = scope_.getIRVar(node.name_);
        int id = -1;
        if (!currentBlock_.belong_.args_.isEmpty() && currentBlock_.belong_.args_.get(0).name_.equals("this")) {
            IRStructType struct = getCurrentStructDef().struct_;
            if (struct.varToIdMap_.containsKey(node.name_)) {
                id = struct.varToIdMap_.get(node.name_);
            }
        }
        if (newVar instanceof IRLocalVar) {
            currentBlock_.result_ = newVar;
            return;
        }
        if (id != -1) {
            newVar = IRLocalVar.newLocalVar(new IRPtrType(IRType.toIRType(node.type_)));
            currentBlock_.instList_.add(
                    new IRGetElementPtrInst((IRLocalVar) newVar, currentBlock_.belong_.args_.get(0), id));
            currentBlock_.result_ = newVar;
            return;
        }
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(IntLiteralNode node) {
        currentBlock_.result_ = new IRIntConst(node.value_);
    }

    @Override
    public void visit(NullLiteralNode node) {
        currentBlock_.result_ = new IRNullConst();
    }

    @Override
    public void visit(StringLiteralNode node) {
        int id = stringCnt_++;
        IRGlobalVar newVar = new IRGlobalVar("string." + id, new IRPtrType(new IRIntType(8)));
        irProgram_.stringLiteralList_.add(new IRStringLiteralDef(newVar, node.value_));
        currentBlock_.result_ = newVar;
    }

    @Override
    public void visit(ThisNode node) {
        currentBlock_.result_ = currentBlock_.belong_.args_.get(0);
    }

    private void addVarAndAssign(String varName, IRLocalVar value) {
        IRLocalVar newVar = (IRLocalVar) scope_.irAddVar(varName, new IRPtrType(value.type_));
        currentBlock_.instList_.add(new IRAllocaInst(newVar));
        currentBlock_.instList_.add(new IRStoreInst(value, newVar));
    }

    private IRStructDef getCurrentStructDef() {
        return irProgram_.structDefMap_.get(scope_.getClassName());
    }

    private IRValue getValueResult(boolean isLeftValue) {
        if (!isLeftValue) {
            return currentBlock_.result_;
        }
        IRLocalVar newVar = IRLocalVar.newLocalVar(((IRPtrType) currentBlock_.result_.type_).getDereferenceType());
        currentBlock_.instList_.add(new IRLoadInst(newVar, currentBlock_.result_));
        return newVar;
    }

    private IRLocalVar generateArray(ArrayList<IRValue> fixedSizeList, int start, IRPtrType type) {
        IRLocalVar newVar = IRLocalVar.newLocalVar(type);
        currentBlock_.instList_.add(
                new IRCallInst(newVar, "builtin.calloc_array", new IRIntConst(type.getDereferenceType().getSize()),
                               fixedSizeList.get(start)));
        if (start == fixedSizeList.size() - 1) {
            return newVar;
        }
        int for_id = newCnt_++;
        IRBasicBlock cond = new IRBasicBlock("new_array_for_cond_" + for_id, currentBlock_.belong_);
        IRBasicBlock body = new IRBasicBlock("new_array_for_body_" + for_id, currentBlock_.belong_);
        IRBasicBlock step = new IRBasicBlock("new_array_for_step_" + for_id, currentBlock_.belong_);
        IRBasicBlock end = new IRBasicBlock("new_array_for_end_" + for_id, currentBlock_.belong_);
        IRLocalVar loopVar = IRLocalVar.newLocalVar(new IRPtrType(new IRIntType(32)));
        currentBlock_.instList_.add(new IRAllocaInst(loopVar));
        currentBlock_.instList_.add(new IRStoreInst(new IRIntConst(0), loopVar));
        currentBlock_.instList_.add(new IRJumpInst(cond));
        submitBlock();
        currentBlock_ = cond;
        IRLocalVar loopVarValue = IRLocalVar.newLocalVar(new IRIntType(32));
        currentBlock_.instList_.add(new IRLoadInst(loopVarValue, loopVar));
        IRLocalVar condValue = IRLocalVar.newLocalVar(new IRIntType(1));
        currentBlock_.instList_.add(new IRIcmpInst(condValue, loopVarValue, fixedSizeList.get(start), "slt"));
        currentBlock_.instList_.add(new IRBrInst(condValue, body, end));
        submitBlock();
        currentBlock_ = body;
        IRLocalVar elemPtr = IRLocalVar.newLocalVar(type);
        currentBlock_.instList_.add(new IRGetElementPtrInst(elemPtr, newVar, loopVarValue));
        IRLocalVar subArray = generateArray(fixedSizeList, start + 1, (IRPtrType) type.getDereferenceType());
        currentBlock_.instList_.add(new IRStoreInst(subArray, elemPtr));
        currentBlock_.instList_.add(new IRJumpInst(step));
        submitBlock();
        currentBlock_ = step;
        IRLocalVar tmp1 = IRLocalVar.newLocalVar(new IRIntType(32));
        currentBlock_.instList_.add(new IRLoadInst(tmp1, loopVar));
        IRLocalVar tmp2 = IRLocalVar.newLocalVar(new IRIntType(32));
        currentBlock_.instList_.add(new IRBinaryInst(tmp2, tmp1, new IRIntConst(1), "add"));
        currentBlock_.instList_.add(new IRStoreInst(tmp2, loopVar));
        currentBlock_.instList_.add(new IRJumpInst(cond));
        submitBlock();
        currentBlock_ = end;
        return newVar;
    }

    private IRLocalVar initArrayLiteral(ArrayLiteralNode node, IRPtrType type) {
        IRLocalVar newVar = IRLocalVar.newLocalVar(type);
        currentBlock_.instList_.add(
                new IRCallInst(newVar, "builtin.malloc_array", new IRIntConst(type.getDereferenceType().getSize()),
                               new IRIntConst(node.elemList_.size())));
        for (int i = 0; i < node.elemList_.size(); i++) {
            IRLocalVar elemPtr = IRLocalVar.newLocalVar(type);
            currentBlock_.instList_.add(new IRGetElementPtrInst(elemPtr, newVar, new IRIntConst(i)));
            if (node.elemList_.get(i) instanceof ArrayLiteralNode) {
                IRLocalVar elemValue = initArrayLiteral((ArrayLiteralNode) node.elemList_.get(i),
                                                        (IRPtrType) type.getDereferenceType());
                currentBlock_.instList_.add(new IRStoreInst(elemValue, elemPtr));
                continue;
            }
            if (node.elemList_.get(i) instanceof NullLiteralNode) {
                currentBlock_.instList_.add(new IRStoreInst(new IRIntConst(0), elemPtr));
                continue;
            }
            node.elemList_.get(i).accept(this);
            currentBlock_.instList_.add(new IRStoreInst(currentBlock_.result_, elemPtr));
        }
        return newVar;
    }

    private void submitBlock() {
        currentBlock_.belong_.body_.add(currentBlock_);
        endBlock_ = true;
    }
}
