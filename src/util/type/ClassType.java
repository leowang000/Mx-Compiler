package util.type;

import java.util.HashMap;

import AST.module.ClassDefNode;

public class ClassType {
    public HashMap<String, FuncType> funcMap_;
    public HashMap<String, Type> varMap_;

    public ClassType() {
        funcMap_ = new HashMap<>();
        varMap_ = new HashMap<>();
    }

    public ClassType(HashMap<String, FuncType> funcMap, HashMap<String, Type> varMap) {
        funcMap_ = funcMap;
        varMap_ = varMap;
    }

    public ClassType(ClassDefNode classDef) {
        funcMap_ = new HashMap<>();
        varMap_ = new HashMap<>();
        for (var varDef : classDef.varDefList_) {
            for (var varDefUnit : varDef.varList_) {
                varMap_.put(varDefUnit.first_, new Type(varDef.type_));
            }
        }
        for (var funcDef : classDef.funcDefList_) {
            funcMap_.put(funcDef.name_, new FuncType(funcDef));
        }
    }
}
