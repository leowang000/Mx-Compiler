package util.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import IR.type.IRType;
import IR.value.IRValue;
import IR.value.var.IRGlobalVar;
import util.Position;
import util.error.*;
import util.type.*;

public class GlobalScope extends ClassScope {
    private final HashMap<String, ClassType> classDefMap_ = new HashMap<>();

    public GlobalScope() {
        super(null, null);
        funcDefMap_.put("print", new FuncType(new Type("void"), new Type("string")));
        funcDefMap_.put("println", new FuncType(new Type("void"), new Type("string")));
        funcDefMap_.put("printInt", new FuncType(new Type("void"), new Type("int")));
        funcDefMap_.put("printlnInt", new FuncType(new Type("void"), new Type("int")));
        funcDefMap_.put("getString", new FuncType(new Type("string")));
        funcDefMap_.put("getInt", new FuncType(new Type("int")));
        funcDefMap_.put("toString", new FuncType(new Type("string"), new Type("int")));
        classDefMap_.put("bool", new ClassType());
        classDefMap_.put("int", new ClassType());
        HashMap<String, FuncType> stringFuncMap = new HashMap<>();
        stringFuncMap.put("length", new FuncType(new Type("int")));
        stringFuncMap.put("substring", new FuncType(new Type("string"), new Type("int"),
                                                    new Type("int")));
        stringFuncMap.put("parseInt", new FuncType(new Type("int")));
        stringFuncMap.put("ord", new FuncType(new Type("int"), new Type("int")));
        classDefMap_.put("string", new ClassType(stringFuncMap, new HashMap<>()));
    }

    @Override
    public IRValue irAddVar(String varName, IRType type) {
        IRGlobalVar globalVar =  new IRGlobalVar(varName, type);
        irVarMap_.put(varName, globalVar);
        return globalVar;
    }

    @Override
    public void addFunc(String funcName, FuncType funcType, Position pos) {
        if (classDefMap_.containsKey(funcName)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Symbol Redefinition Error: " + funcName, pos);
        }
        super.addFunc(funcName, funcType, pos);
    }

    public void addClass(String className, ClassType classType, Position pos) {
        if (funcDefMap_.containsKey(className)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Symbol Redefinition Error: " + className, pos);
        }
        if (classDefMap_.containsKey(className)) {
            System.out.println("Multiple Definitions");
            throw new SyntaxError("Class Redefinition Error: " + className, pos);
        }
        classDefMap_.put(className, classType);
    }

    @Override
    public boolean isInLoop() {
        return false;
    }

    @Override
    public String getClassName() {
        return null;
    }

    @Override
    public Type getReturnType() {
        return null;
    }

    @Override
    public Type getVarType(String varName) {
        if (varDefMap_.containsKey(varName)) {
            return varDefMap_.get(varName);
        }
        return null;
    }

    @Override
    public FuncType getFuncType(String funcName) {
        if (funcDefMap_.containsKey(funcName)) {
            return funcDefMap_.get(funcName);
        }
        return null;
    }

    public ClassType getClassType(String className) {
        if (classDefMap_.containsKey(className)) {
            return classDefMap_.get(className);
        }
        return null;
    }

    @Override
    public IRValue getIRVar(String varName) {
        if (irVarMap_.containsKey(varName)) {
            return irVarMap_.get(varName);
        }
        return null;
    }
}
