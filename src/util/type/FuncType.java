package util.type;

import java.util.ArrayList;

import AST.def.FuncDefNode;

public class FuncType {
    public Type returnType_;
    ArrayList<Type> argTypes_;

    public FuncType(Type returnType){
        returnType_ = returnType;
        argTypes_ = new ArrayList<>();
    }

    public FuncType(Type returnType, ArrayList<Type> argTypes) {
        returnType_ = returnType;
        argTypes_ = argTypes;
    }

    public FuncType(FuncDefNode funcDef) {
        returnType_ = new Type(funcDef.returnType_);
        argTypes_ = new ArrayList<>();
        for (var param : funcDef.paramList_) {
            argTypes_.add(param.first_);
        }
    }
}
