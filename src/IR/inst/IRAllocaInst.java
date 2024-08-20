package IR.inst;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.type.IRType;
import IR.value.var.IRLocalVar;

public class IRAllocaInst extends IRInst {
    public IRLocalVar result_;

    public IRAllocaInst(String resultName, IRType type) {
        result_ = new IRLocalVar(resultName, new IRPtrType(type));
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", result_, ((IRPtrType) result_.type_).base_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
