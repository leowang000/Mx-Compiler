package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.value.var.IRLocalVar;

public class IRAllocaInst extends IRInst {
    public IRLocalVar result_;

    public IRAllocaInst(IRLocalVar result) {
        result_ = result;
        result_.isAllocaResult_ = true;
    }

    @Override
    public String toString() {
        return String.format("%s = alloca %s", result_, ((IRPtrType) result_.type_).getDereferenceType());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void getUseAndDef() {
        use_ = new HashSet<>();
        def_ = new HashSet<>();
        def_.add(result_);
    }
}
