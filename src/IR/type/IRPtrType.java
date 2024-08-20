package IR.type;

import IR.value.IRValue;
import IR.value.constant.IRNullConst;

public class IRPtrType extends IRType {
    public IRType base_;
    public int dim_;

    public IRPtrType() {
        base_ = new IRVoidType();
        dim_ = 1;
    }

    public IRPtrType(IRType base) {
        if (base instanceof IRPtrType) {
            IRPtrType baseType = (IRPtrType) base;
            base_ = baseType.base_;
            dim_ = baseType.dim_ + 1;
        }
        else {
            base_ = base;
            dim_ = 1;
        }
    }

    public IRPtrType(IRType base, int dim) {
        base_ = base;
        dim_ = dim;
    }

    @Override
    public String toString() {
        return "ptr";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IRPtrType)) {
            return false;
        }
        IRPtrType other = (IRPtrType) obj;
        if (base_ == null) {
            return other.base_ == null;
        }
        return base_.equals(other.base_) && dim_ == other.dim_;
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public IRValue getDefaultValue() {
        return new IRNullConst();
    }
}
