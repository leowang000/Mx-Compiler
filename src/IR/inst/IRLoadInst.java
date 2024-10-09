package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRLoadInst extends IRInst {
    public IRLocalVar result_;
    public IRValue pointer_;

    public IRLoadInst(IRLocalVar result, IRValue pointer) {
        result_ = result;
        pointer_ = pointer;
    }

    @Override
    public String toString() {
        return String.format("%s = load %s, ptr %s", result_, result_.type_, pointer_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        HashSet<IRLocalVar> use = new HashSet<>();
        addVar(use, pointer_);
        return use;
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>();
    }
}
