package IR.inst;

import java.util.ArrayList;
import java.util.Arrays;

import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRCallInst extends IRInst {
    public IRLocalVar result_;
    public String funcName_;
    public ArrayList<IRValue> args_;

    public IRCallInst(IRLocalVar result, String funcName, IRValue ... args) {
        result_ = result;
        funcName_ = funcName;
        args_ = new ArrayList<>();
        args_.addAll(Arrays.asList(args));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (result_ == null) {
            sb.append("call void");
        }
        else {
            sb.append(result_).append(" = call ").append(result_.type_);
        }
        sb.append(" @").append(funcName_).append("(");
        for (int i = 0; i < args_.size(); i++) {
            sb.append(args_.get(i).type_).append(" ").append(args_.get(i));
            if (i < args_.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
