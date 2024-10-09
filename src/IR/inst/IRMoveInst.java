package IR.inst;

import java.util.HashSet;
import java.util.List;

import IR.IRVisitor;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRMoveInst extends IRInst {
    public IRLocalVar dest_;
    public IRValue src_;

    public IRMoveInst(IRLocalVar dest, IRValue src) {
        dest_ = dest;
        src_ = src;
    }

    @Override
    public String toString() {
        return String.format("%s\t<= %s", dest_, src_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        HashSet<IRLocalVar> use = new HashSet<>();
        addVar(use, src_);
        return use;
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>(List.of(dest_));
    }
}
