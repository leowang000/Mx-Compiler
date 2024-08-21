package IR.type;

import IR.value.IRValue;

public abstract class IRType {
    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    public abstract int getSize();
}
