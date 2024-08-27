package IR.module;

import java.util.ArrayList;
import java.util.HashSet;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRType;
import IR.value.var.IRLocalVar;

public class IRFuncDef extends IRNode {
    public String name_;
    public IRType returnType_;
    public ArrayList<IRLocalVar> args_;
    public ArrayList<IRBasicBlock> body_;
    public int stackSize_ = 0, maxFuncArgCnt_ = 0;
    public HashSet<IRLocalVar> localVarSet_;

    public IRFuncDef(String name, IRType returnType) {
        name_ = name;
        returnType_ = returnType;
        args_ = new ArrayList<>();
        body_ = new ArrayList<>();
        localVarSet_ = new HashSet<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(returnType_).append(" @").append(name_).append("(");
        for (int i = 0; i < args_.size(); i++) {
            sb.append(args_.get(i).type_).append(" ").append(args_.get(i));
            if (i < args_.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {\n");
        for (var block : body_) {
            sb.append(block);
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
