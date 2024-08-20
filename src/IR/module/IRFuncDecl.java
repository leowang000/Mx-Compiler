package IR.module;

import java.util.ArrayList;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRType;

public class IRFuncDecl extends IRNode {
    public String name_;
    public IRType returnType_;
    public ArrayList<IRType> argTypeList_;

    public IRFuncDecl(String name, IRType returnType) {
        name_ = name;
        returnType_ = returnType;
        argTypeList_ = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare ").append(returnType_).append(" @").append(name_).append("(");
        for (int i = 0; i < argTypeList_.size(); i++) {
            sb.append(argTypeList_.get(i));
            if (i < argTypeList_.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")\n");
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
