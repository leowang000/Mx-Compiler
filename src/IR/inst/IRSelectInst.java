package IR.inst;

import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRSelectInst extends IRInst {
    public IRLocalVar result_;
    public IRValue cond_, val1_, val2_;

    public IRSelectInst(String resultName, IRValue cond, IRValue val1, IRValue val2) {
        result_ = new IRLocalVar(resultName, val1.type_);
        cond_ = cond;
        val1_ = val1;
        val2_ = val2;
    }

    @Override
    public String toString() {
        return String.format("%s = select i1 %s, %s %s, %s %s", result_, cond_, val1_.type_, val1_, val2_.type_, val2_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
