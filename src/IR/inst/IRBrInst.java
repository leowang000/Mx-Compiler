package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRBrInst extends IRInst {
    public IRValue cond_;
    public IRBasicBlock trueBlock_, falseBlock_;

    public IRBrInst(IRValue cond, IRBasicBlock trueBlock, IRBasicBlock falseBlock) {
        cond_ = cond;
        trueBlock_ = trueBlock;
        falseBlock_ = falseBlock;
    }

    @Override
    public String toString() {
        return String.format("br i1 %s, label %s, label %s", cond_, trueBlock_.getLabel(), falseBlock_.getLabel());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public HashSet<IRLocalVar> getUse() {
        HashSet<IRLocalVar> use = new HashSet<>();
        addVar(use, cond_);
        return use;
    }

    @Override
    public HashSet<IRLocalVar> getDef() {
        return new HashSet<>();
    }
}
