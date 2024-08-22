package IR.value.var;

import IR.type.IRType;
import IR.value.IRValue;

public class IRLocalVar extends IRValue {
    public String name_;
    public static int cnt_;

    static {
        cnt_ = 0;
    }

    public IRLocalVar(String name, IRType type) {
        super(type);
        name_ = name;
    }

    @Override
    public String toString() {
        return "%" + name_;
    }

    public static IRLocalVar newLocalVar(IRType type) {
        return new IRLocalVar(String.format("%d", cnt_++), type);
    }
}
