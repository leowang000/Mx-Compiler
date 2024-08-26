package asm.inst;

public class ASMCallInst extends ASMInst {
    public String label_;

    public ASMCallInst(String label) {
        label_ = label;
    }

    @Override
    public String toString() {
        return String.format("call\t%s", label_);
    }
}
