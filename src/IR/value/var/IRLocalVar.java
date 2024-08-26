package IR.value.var;

import IR.type.IRType;
import IR.value.IRValue;
import asm.util.Register;

public class IRLocalVar extends IRValue {
    public String name_, register_ = null;
    public boolean isAnonymous_;
    public int stackOffset_ = 0;
    public static int cnt_ = 0;

    public IRLocalVar(String name, IRType type) {
        super(type);
        name_ = name;
        try {
            Integer.parseInt(name_);
            isAnonymous_ = true;
        }
        catch (NumberFormatException e) {
            isAnonymous_ = false;
        }
    }

    @Override
    public String toString() {
        return "%" + name_;
    }

    public static IRLocalVar newLocalVar(IRType type) {
        return new IRLocalVar(String.format("%d", cnt_++), type);
    }
}
