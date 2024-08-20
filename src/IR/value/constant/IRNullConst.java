package IR.value.constant;

import IR.type.IRPtrType;
import IR.value.IRValue;

public class IRNullConst extends IRValue {
    public IRNullConst() {
        super(new IRPtrType());
    }

    @Override
    public String toString() {
        return "null";
    }
}
