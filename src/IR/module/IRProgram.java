package IR.module;

import java.util.ArrayList;
import java.util.HashMap;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRIntType;
import IR.type.IRPtrType;
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
        funcDeclList_.add(new IRFuncDecl("print", new IRVoidType(), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("println", new IRVoidType(), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("printInt", new IRVoidType(), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("printlnInt", new IRVoidType(), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("getString", new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("getInt", new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("toString", new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("array.copy", new IRVoidType(), new IRPtrType(), new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("array.size", new IRIntType(32), new IRPtrType()));
        funcDeclList_.add(
                new IRFuncDecl("string.copy", new IRPtrType(new IRIntType(8)), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("string.length", new IRIntType(32), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("string.ord", new IRIntType(32), new IRPtrType(new IRIntType(8)), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("string.parseInt", new IRIntType(32), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("string.substring", new IRPtrType(new IRIntType(8)), new IRPtrType(new IRIntType(8)),
                               new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("builtin.bool_to_string", new IRPtrType(new IRIntType(8)), new IRIntType(1)));
        funcDeclList_.add(
                new IRFuncDecl("builtin.malloc_array", new IRPtrType(), new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(
                new IRFuncDecl("builtin.string_cat", new IRPtrType(new IRIntType(8)), new IRPtrType(new IRIntType(8)),
                               new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_eq", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_ge", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_geq", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_le", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_leq", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.string_ne", new IRIntType(1), new IRPtrType(new IRIntType(8)),
                                         new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("builtin.init", new IRVoidType()));
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
