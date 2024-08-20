package IR.inst;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.value.var.IRLocalVar;

public class IRBrInst extends IRInst {
    public IRLocalVar cond_;
    public IRBasicBlock thenBlock_, elseBlock_;

    public IRBrInst(IRLocalVar cond, IRBasicBlock thenBlock, IRBasicBlock elseBlock) {
        cond_ = cond;
        thenBlock_ = thenBlock;
        elseBlock_ = elseBlock;
    }

    @Override
    public String toString() {
        return String.format("br i1 %s, label %s, label %s", cond_, thenBlock_.label_, elseBlock_.label_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
