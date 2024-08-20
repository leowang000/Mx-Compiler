package IR;

public abstract class IRNode {
    @Override
    public abstract String toString();

    public abstract void accept(IRVisitor visitor);
}
