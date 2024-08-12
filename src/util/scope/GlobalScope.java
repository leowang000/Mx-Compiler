package util.scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.Position;
import util.error.SemanticError;
import util.type.ClassType;
import util.type.FuncType;
import util.type.Type;

public class GlobalScope extends ClassScope {
    HashMap<String, ClassType> classDefMap_;

    public GlobalScope() {
        super(null, null);
        funcDefMap_.put("print", new FuncType(new Type("void"),
                                              new ArrayList<>(List.of(new Type("string")))));
        funcDefMap_.put("println", new FuncType(new Type("void"),
                                                new ArrayList<>(List.of(new Type("string")))));
        funcDefMap_.put("printInt", new FuncType(new Type("void"),
                                                 new ArrayList<>(List.of(new Type("int")))));
        funcDefMap_.put("printlnInt", new FuncType(new Type("void"),
                                                   new ArrayList<>(List.of(new Type("int")))));
        funcDefMap_.put("getString", new FuncType(new Type("string")));
        funcDefMap_.put("getInt", new FuncType(new Type("int")));
        funcDefMap_.put("toString", new FuncType(new Type("string"),
                                                 new ArrayList<>(List.of(new Type("int")))));
        classDefMap_ = new HashMap<>();
        classDefMap_.put("bool", new ClassType());
        classDefMap_.put("int", new ClassType());
        HashMap<String, FuncType> stringFuncMap = new HashMap<>();
        stringFuncMap.put("length", new FuncType(new Type("int")));
        stringFuncMap.put("substring", new FuncType(new Type("string"),
                                                    new ArrayList<>(Arrays.asList(new Type("int"),
                                                                                  new Type("int")))));
        stringFuncMap.put("parseInt", new FuncType(new Type("int")));
        stringFuncMap.put("ord", new FuncType(new Type("int"), new ArrayList<>(List.of(new Type("int")))));
        classDefMap_.put("string", new ClassType(stringFuncMap, new HashMap<>()));
    }

    @Override
    public void addFunc(String funcName, FuncType funcType, Position pos) {
        if (classDefMap_.containsKey(funcName)) {
            throw new SemanticError("Symbol Redefinition Error: " + funcName, pos);
        }
        super.addFunc(funcName, funcType, pos);
    }

    public void addClass(String className, ClassType classType, Position pos) {
        if (funcDefMap_.containsKey(className)) {
            throw new SemanticError("Symbol Redefinition Error: " + className, pos);
        }
        if (classDefMap_.containsKey(className)) {
            throw new SemanticError("Class Redefinition Error: " + className, pos);
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
    public void checkReturnType(Type type, Position pos) {
        throw new SemanticError("Unexpected Return Error", pos);
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
}
