package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.value.IRValue;

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
    public void getUseAndDef() {
        use_ = new HashSet<>();
        def_ = new HashSet<>();
        addUseVar(value_);
        addUseVar(pointer_);
    }
}
