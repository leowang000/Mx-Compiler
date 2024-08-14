package util.scope;

import java.util.HashMap;

import util.Position;
import util.error.SyntaxError;
import util.type.FuncType;
import util.type.Type;

public class Scope {
    public Scope parent_;
    public HashMap<String, Type> varDefMap_;

    public Scope(Scope parent) {
        parent_ = parent;
        varDefMap_ = new HashMap<>();
    }

    public void addVar(String varName, Type type, Position pos) {
        if (varDefMap_.containsKey(varName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Variable Redefinition Error: " + varName, pos);
        }
        varDefMap_.put(varName, type);
    }

    public boolean isInLoop() {
        return parent_.isInLoop();
    }

    public String getClassName() {
        return parent_.getClassName();
    }

    public void checkReturnType(Type type, Position pos) {
        parent_.checkReturnType(type, pos);
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
}
