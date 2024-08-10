package util.type;

import java.util.ArrayList;

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
}
