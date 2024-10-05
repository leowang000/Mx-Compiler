package util.scope;

import java.util.HashMap;

import util.Position;
import util.error.SyntaxError;
import util.type.FuncType;
import util.type.Type;

public class ClassScope extends Scope {
    public String name_;
    public HashMap<String, FuncType> funcDefMap_ = new HashMap<>();

    public ClassScope(Scope parent, String name) {
        super(parent);
        name_ = name;
    }

    @Override
    public void addVar(String varName, Type type, Position pos) {
        if (funcDefMap_.containsKey(varName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Symbol Redefinition Error: " + varName, pos);
        }
        super.addVar(varName, type, pos);
    }

    public void addFunc(String funcName, FuncType funcType, Position pos) {
        if (funcName.equals(name_)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Symbol Redefinition Error: " + funcName, pos);
        }
        if (varDefMap_.containsKey(funcName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Symbol Redefinition Error: " + funcName, pos);
        }
        if (funcDefMap_.containsKey(funcName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Function Redefinition Error: " + funcName, pos);
        }
        funcDefMap_.put(funcName, funcType);
    }

    @Override
    public String getClassName() {
        return name_;
    }

    @Override
    public FuncType getFuncType(String funcName) {
        if (funcDefMap_.containsKey(funcName)) {
            return funcDefMap_.get(funcName);
        }
        return parent_.getFuncType(funcName);
    }
}
