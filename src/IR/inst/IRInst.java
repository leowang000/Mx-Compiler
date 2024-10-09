package IR.inst;

import java.util.HashSet;

import IR.IRNode;
import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public abstract class IRInst extends IRNode {
    @Override
    public abstract String toString();

    @Override
    public abstract void accept(IRVisitor visitor);

    public abstract HashSet<IRLocalVar> getUse();

    public abstract HashSet<IRLocalVar> getDef();

    public static void addVar(HashSet<IRLocalVar> dest, IRValue value) {
        if (value instanceof IRLocalVar) {
            dest.add((IRLocalVar) value);
        }
    }
}
