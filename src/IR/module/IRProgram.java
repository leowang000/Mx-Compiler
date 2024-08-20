package IR.module;

import java.util.ArrayList;
import java.util.HashMap;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRVoidType;

public class IRProgram extends IRNode {
    public ArrayList<IRStructDef> structList_;
    public ArrayList<IRGlobalVarDef> globalVarList_;
    public ArrayList<IRStringLiteralDef> stringList_;
    public ArrayList<IRGlobalVarDef> arrayList_;
    public ArrayList<IRFuncDecl> funcDeclList_;
    public HashMap<String, IRFuncDef> funcDefMap_;

    public IRProgram() {
        structList_ = new ArrayList<>();
        globalVarList_ = new ArrayList<>();
        stringList_ = new ArrayList<>();
        arrayList_ = new ArrayList<>();
        funcDeclList_ = new ArrayList<>();
        funcDefMap_ = new HashMap<>();
        // TODO: Add Declarations of Builtin Functions
        funcDeclList_.add(new IRFuncDecl("..init", new IRVoidType()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (var struct : structList_) {
            sb.append(struct);
        }
        for (var globalVar : globalVarList_) {
            sb.append(globalVar);
        }
        for (var stringLiteral : stringList_) {
            sb.append(stringLiteral);
        }
        for (var arrayLiteral : arrayList_) {
            sb.append(arrayLiteral);
        }
        for (var funcDecl : funcDeclList_) {
            sb.append(funcDecl);
        }
        for (var funcDef : funcDefMap_.values()) {
            sb.append(funcDef);
        }
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
