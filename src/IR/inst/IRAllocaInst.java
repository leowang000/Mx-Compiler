package IR.inst;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.type.IRType;
import IR.value.var.IRLocalVar;

public class IRAllocaInst extends IRInst {
    public IRLocalVar result_;

    public IRAllocaInst(IRLocalVar result) {
        result_ = result;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", result_, ((IRPtrType) result_.type_).getDereferenceType());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
