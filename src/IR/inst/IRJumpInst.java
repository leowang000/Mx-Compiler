package IR.inst;

import IR.IRVisitor;
import IR.module.IRBasicBlock;

public class IRJumpInst extends IRInst {
    public IRBasicBlock destBlock_;

    public IRJumpInst(IRBasicBlock destBlock) {
        destBlock_ = destBlock;
    }

    @Override
    public String toString() {
        return String.format("br label %s", destBlock_.label_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
