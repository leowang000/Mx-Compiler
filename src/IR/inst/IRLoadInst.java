package IR.inst;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRLoadInst extends IRInst {
    public IRLocalVar result_;
    public IRValue pointer_;

    public IRLoadInst(String resultName, IRValue pointer) {
        result_ = new IRLocalVar(resultName, ((IRPtrType) pointer.type_).base_);
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
}
