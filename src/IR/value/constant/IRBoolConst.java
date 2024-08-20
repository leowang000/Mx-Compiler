package IR.value.constant;

import IR.type.IRIntType;
import IR.value.IRValue;

public class IRBoolConst extends IRValue {
    public boolean value_;

    public IRBoolConst(boolean value) {
        super(new IRIntType(1));
        value_ = value;
    }

    @Override
    public String toString() {
        return value_ ? "true" : "false";
    }
}
