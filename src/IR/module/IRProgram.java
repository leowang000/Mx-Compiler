package IR.module;

import java.util.ArrayList;
import java.util.HashMap;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.*;

public class IRProgram extends IRNode {
    public HashMap<String, IRStructDef> structDefMap_ = new HashMap<>();
    public ArrayList<IRGlobalVarDef> globalVarList_ = new ArrayList<>();
    public ArrayList<IRStringLiteralDef> stringLiteralList_ = new ArrayList<>();
    public ArrayList<IRStringLiteralDef> fStringList_ = new ArrayList<>();
    public ArrayList<IRGlobalVarDef> arrayLiteralList_ = new ArrayList<>();
    public ArrayList<IRFuncDecl> funcDeclList_ = new ArrayList<>();
    public HashMap<String, IRFuncDef> funcDefMap_ = new HashMap<>();

    public IRProgram() {
        funcDeclList_.add(new IRFuncDecl("print", new IRVoidType(), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("println", new IRVoidType(), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("printInt", new IRVoidType(), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("printlnInt", new IRVoidType(), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("getString", new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(new IRFuncDecl("getInt", new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("toString", new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("array.copy", new IRPtrType(), new IRPtrType(), new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("array.size", new IRIntType(32), new IRPtrType()));
        funcDeclList_.add(new IRFuncDecl("string.length", new IRIntType(32), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("string.ord", new IRIntType(32), new IRPtrType(new IRIntType(8)), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("string.parseInt", new IRIntType(32), new IRPtrType(new IRIntType(8))));
        funcDeclList_.add(
                new IRFuncDecl("string.substring", new IRPtrType(new IRIntType(8)), new IRPtrType(new IRIntType(8)),
                               new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("builtin.bool_to_string", new IRPtrType(new IRIntType(8)), new IRIntType(1)));
        funcDeclList_.add(new IRFuncDecl("builtin.calloc", new IRPtrType(), new IRIntType(32)));
        funcDeclList_.add(
                new IRFuncDecl("builtin.calloc_array", new IRPtrType(), new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(new IRFuncDecl("builtin.malloc", new IRPtrType(), new IRIntType(32)));
        funcDeclList_.add(
                new IRFuncDecl("builtin.malloc_array", new IRPtrType(), new IRIntType(32), new IRIntType(32)));
        funcDeclList_.add(
                new IRFuncDecl("builtin.string_add", new IRPtrType(new IRIntType(8)), new IRPtrType(new IRIntType(8)),
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
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(";Definition Of Structs\n");
        for (var struct : structDefMap_.values()) {
            sb.append(struct);
        }
        sb.append("\n;Definition Of Global Variables\n");
        for (var globalVar : globalVarList_) {
            sb.append(globalVar);
        }
        sb.append("\n;Definition Of String Literals\n");
        for (var stringLiteral : stringLiteralList_) {
            sb.append(stringLiteral);
        }
        sb.append("\n;Definition Of Formatted String Fragments\n");
        for (var fString : fStringList_) {
            sb.append(fString);
        }
        sb.append("\n;Definition Of Array Literals\n");
        for (var arrayLiteral : arrayLiteralList_) {
            sb.append(arrayLiteral);
        }
        sb.append("\n;Declarations Of Functions\n");
        for (var funcDecl : funcDeclList_) {
            sb.append(funcDecl);
        }
        sb.append("\n;Definition Of Functions\n");
        for (var funcDef : funcDefMap_.values()) {
            sb.append(funcDef).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    public void reset() {
        for (var funcDef : funcDefMap_.values()) {
            for (var block : funcDef.body_) {
                block.preds_.clear();
                block.succs_.clear();
                block.idom_ = null;
                block.domChildren_.clear();
                block.domFrontiers_.clear();
                for (var inst : block.phiMap_.values()) {
                    inst.use_.clear();
                    inst.def_.clear();
                    inst.in_.clear();
                    inst.out_.clear();
                }
                for (var inst : block.instList_) {
                    inst.use_.clear();
                    inst.def_.clear();
                    inst.in_.clear();
                    inst.out_.clear();
                }
            }
        }
    }
}
