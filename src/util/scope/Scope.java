package util.scope;

import java.util.HashMap;

import IR.type.IRType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;
import util.Position;
import util.error.SyntaxError;
import util.type.FuncType;
import util.type.Type;

public class Scope {
    public Scope parent_;
    public HashMap<String, Type> varDefMap_ = new HashMap<>();
    public HashMap<String, IRValue> irVarMap_ = new HashMap<>();
    private static final HashMap<String, Integer> varCntMap_ = new HashMap<>();

    public Scope(Scope parent) {
        parent_ = parent;
    }

    public void addVar(String varName, Type type, Position pos) {
        if (varDefMap_.containsKey(varName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Variable Redefinition Error: " + varName, pos);
        }
        varDefMap_.put(varName, type);
    }

    public IRValue irAddVar(String varName, IRType type) {
        int id;
        if (varCntMap_.containsKey(varName)) {
            id = varCntMap_.get(varName);
            varCntMap_.put(varName, id + 1);
        }
        else {
            id = 0;
            varCntMap_.put(varName, 1);
        }
        IRLocalVar localVar = new IRLocalVar(String.format("%s.%d", varName, id), type);
        irVarMap_.put(varName, localVar);
        return localVar;
    }

    public boolean isInLoop() {
        return parent_.isInLoop();
    }

    public String getClassName() {
        return parent_.getClassName();
    }

    public Type getReturnType() {
        return parent_.getReturnType();
    }

    public Type getVarType(String varName) {
        if (varDefMap_.containsKey(varName)) {
            return varDefMap_.get(varName);
        }
        return parent_.getVarType(varName);
    }

    public FuncType getFuncType(String funcName) {
        return parent_.getFuncType(funcName);
    }

    public IRValue getIRVar(String varName) {
        if (irVarMap_.containsKey(varName)) {
            return irVarMap_.get(varName);
        }
        return parent_.getIRVar(varName);
    }
}
