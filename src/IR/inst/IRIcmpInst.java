package IR.inst;

import IR.IRVisitor;
import IR.type.IRIntType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRIcmpInst extends IRInst {
    public IRLocalVar result_;
    public IRValue lhs_, rhs_;
    public String cond_;

    public IRIcmpInst(IRLocalVar result, IRValue lhs, IRValue rhs, String cond) {
        result_ = result;
        lhs_ = lhs;
        rhs_ = rhs;
        cond_ = cond;
    }

    @Override
    public String toString() {
        return String.format("%s = icmp %s %s %s, %s", result_, cond_, lhs_.type_, lhs_, rhs_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
