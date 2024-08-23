package IR.type;

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
        if (base instanceof IRPtrType) {
            IRPtrType baseType = (IRPtrType) base;
            base_ = baseType.base_;
            dim_ = baseType.dim_ + dim;
        }
        else {
            base_ = base;
            dim_ = dim;
        }
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
        return base_.equals(other.base_) && dim_ == other.dim_;
    }

    @Override
    public int getSize() {
        return 4;
    }

    public IRType getDereferenceType() {
        return dim_ == 1 ? base_ : new IRPtrType(base_, dim_ - 1);
    }
}
