package IR.value.constant;

import IR.type.IRIntType;
import IR.value.IRValue;

public class IRIntConst extends IRValue {
    public int value_;

    public IRIntConst(int value) {
        super(new IRIntType(32));
        value_ = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value_);
    }
}
