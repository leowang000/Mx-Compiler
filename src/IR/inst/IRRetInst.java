package IR.inst;

import IR.IRVisitor;
import IR.value.IRValue;

public class IRRetInst extends IRInst {
    public IRValue value_;

    public IRRetInst(IRValue value) {
        value_ = value;
    }

    @Override
    public String toString() {
        if (value_ == null) {
            return "ret void";
        }
        return String.format("ret %s %s", value_.type_, value_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void getUse() {
        addUseVar(value_);
    }

    @Override
    public void getDef() {}
}
