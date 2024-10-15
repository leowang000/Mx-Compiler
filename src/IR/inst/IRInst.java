package IR.inst;

import java.util.HashSet;

import IR.IRNode;
import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public abstract class IRInst extends IRNode {
    public HashSet<IRLocalVar> use_ = new HashSet<>(), def_ = new HashSet<>();
    public HashSet<IRLocalVar> in_ = new HashSet<>(), out_ = new HashSet<>();

    @Override
    public abstract String toString();

    @Override
    public abstract void accept(IRVisitor visitor);

    public abstract void getUse();

    public abstract void getDef();

    void addUseVar(IRValue value) {
        if (value instanceof IRLocalVar) {
            use_.add((IRLocalVar) value);
        }
    }
}
