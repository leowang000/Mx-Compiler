package util.type;

import java.util.ArrayList;
import java.util.Arrays;

import AST.module.FuncDefNode;

public class FuncType {
    public Type returnType_;
    public ArrayList<Type> argTypeList_;

    public FuncType(Type returnType, Type... argTypes) {
        returnType_ = returnType;
        argTypeList_ = new ArrayList<>();
        argTypeList_.addAll(Arrays.asList(argTypes));
    }

    public FuncType(FuncDefNode funcDef) {
        returnType_ = new Type(funcDef.returnType_);
        argTypeList_ = new ArrayList<>();
        for (var param : funcDef.paramList_) {
            argTypeList_.add(param.first_);
        }
    }
}
