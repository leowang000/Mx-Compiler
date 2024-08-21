package IR.type;

import IR.value.IRValue;
import IR.value.constant.IRBoolConst;
import IR.value.constant.IRIntConst;

public class IRIntType extends IRType {
    public int len_;

    public IRIntType(int len) {
        len_ = len;
    }

    @Override
    public String toString() {
        return "i" + len_;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IRIntType)) {
            return false;
        }
        return len_ == ((IRIntType)obj).len_;
    }

    @Override
    public int getSize() {
        return (len_ + 7) / 8;
    }
}
