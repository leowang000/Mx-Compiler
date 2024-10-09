package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRRetInst extends IRInst {
    public IRValue value_;

    public IRRetInst(IRValue value) {
        value_ = value;
    }

    @Override
    public String toString() {
        if (value_ == null) {
            return "ret void";
        }
        return String.format("ret %s %s", value_.type_, value_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        HashSet<IRLocalVar> use = new HashSet<>();
        addVar(use, value_);
        return use;
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>();
    }
}
