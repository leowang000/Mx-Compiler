package IR.inst;

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
    public void getUse() {
        addUseVar(value_);
        addUseVar(pointer_);
    }

    @Override
    public void getDef() {}
}
