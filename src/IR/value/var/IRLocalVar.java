package IR.value.var;

import IR.type.IRType;
import IR.value.IRValue;
import asm.util.Register;

public class IRLocalVar extends IRValue {
    public String name_;
    public boolean isAllocaResult_ = false;
    public Register register_ = null;
    public int stackOffset_ = -1;
    private static int cnt_ = 0;

    public IRLocalVar(String name, IRType type) {
        super(type);
        name_ = name;
    }

    @Override
    public String toString() {
        return "%" + name_;
    }

    public boolean isUnused() {
        return register_ == null && stackOffset_ == -1;
    }

    public static IRLocalVar newLocalVar(IRType type) {
        return new IRLocalVar(String.format("%d", cnt_++), type);
    }
}
