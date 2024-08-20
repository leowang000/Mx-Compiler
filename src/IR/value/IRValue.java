package IR.value;

import IR.type.IRType;

public abstract class IRValue {
    public IRType type_;

    public IRValue(IRType type) {
        type_ = type;
    }

    @Override
    public abstract String toString();
}
