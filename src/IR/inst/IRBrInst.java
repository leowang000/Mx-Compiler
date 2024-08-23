package IR.inst;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.value.IRValue;

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
}
