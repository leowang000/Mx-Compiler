package IR.inst;

import IR.IRVisitor;
import IR.type.IRIntType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRBinaryInst extends IRInst {
    public IRLocalVar result_;
    public IRValue lhs_, rhs_;
    public String op_;

    public IRBinaryInst(IRLocalVar result, IRValue lhs, IRValue rhs, String op) {
        result_ = result;
        lhs_ = lhs;
        rhs_ = rhs;
        op_ = op;
    }

    @Override
    public String toString() {
        return String.format("%s = %s i32 %s %s", result_, op_, lhs_, rhs_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
