package IR.value.var;

import IR.type.IRType;
import IR.value.IRValue;

public class IRLocalVar extends IRValue {
    public String name_;

    public IRLocalVar(String name, IRType type){
        super(type);
        name_ = name;
    }

    @Override
    public String toString() {
        return "%" + name_;
    }
}
