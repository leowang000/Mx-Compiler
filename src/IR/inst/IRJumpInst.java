package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.module.IRBasicBlock;

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
    public void getUseAndDef() {
        use_ = new HashSet<>();
        def_ = new HashSet<>();
    }
}
