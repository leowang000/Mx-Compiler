package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.value.var.IRLocalVar;

public class IRJumpInst extends IRInst {
    public IRBasicBlock destBlock_;

    public IRJumpInst(IRBasicBlock destBlock) {
        destBlock_ = destBlock;
    }

    @Override
    public String toString() {
        return String.format("br label %s", destBlock_.getLabel());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        return new HashSet<>();
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>();
    }
}
