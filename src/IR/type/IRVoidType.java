package IR.type;

import IR.value.IRValue;

public class IRVoidType extends IRType {
    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IRVoidType;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public IRValue getDefaultValue() {
        return null;
    }
}
