package IR.inst;

import IR.IRVisitor;
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
    public void getUse() {
        addUseVar(pointer_);
    }

    @Override
    public void getDef() {}
}
