package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRStoreInst extends IRInst {
    public IRValue value_, pointer_;

    public IRStoreInst(IRValue value, IRValue pointer) {
        value_ = value;
        pointer_ = pointer;
    }

    @Override
    public String toString() {
        return String.format("store %s %s, ptr %s", value_.type_, value_, pointer_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        HashSet<IRLocalVar> use = new HashSet<>();
        addVar(use, value_);
        addVar(use, pointer_);
        return use;
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>();
    }
}
