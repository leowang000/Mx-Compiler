package IR.inst;

import IR.IRNode;
import IR.IRVisitor;

public abstract class IRInst extends IRNode {
    @Override
    public abstract String toString();

    @Override
    public abstract void accept(IRVisitor visitor);
}
