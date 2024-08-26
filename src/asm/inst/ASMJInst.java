package asm.inst;

public class ASMJInst extends ASMInst {
    public String dest_;

    public ASMJInst(String dest) {
        dest_ = dest;
    }

    @Override
    public String toString() {
        return String.format("j\t%s", dest_);
    }
}
