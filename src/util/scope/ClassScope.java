package util.scope;

import java.util.HashMap;

import util.Position;
import util.error.SemanticError;
import util.type.FuncType;

public class ClassScope extends Scope {
    public HashMap<String, FuncType> funcDefMap_;

    public ClassScope(Scope parent) {
        super(parent);
        funcDefMap_ = new HashMap<>();
    }

    public void addFunc(String funcName, FuncType funcType, Position pos) {
        if (funcDefMap_.containsKey(funcName)) {
            throw new SemanticError("Function Redefinition Error: " + funcName, pos);
        }
        funcDefMap_.put(funcName, funcType);
    }

    @Override
    public boolean isInClass() {
        return true;
    }

    @Override
    public FuncType getFuncType(String funcName) {
        if (funcDefMap_.containsKey(funcName)) {
            return funcDefMap_.get(funcName);
        }
        return parent_.getFuncType(funcName);
    }
}
