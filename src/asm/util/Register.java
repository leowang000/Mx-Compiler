package asm.util;

public class Register {
    public String name_;

    public Register(String name){
        name_ = name;
    }

    @Override
    public String toString() {
        return name_;
    }
}
