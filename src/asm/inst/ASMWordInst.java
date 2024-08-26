package asm.inst;

public class ASMWordInst extends ASMInst {
    public int value_;

    public ASMWordInst(int value) {
        value_ = value;
    }

    @Override
    public String toString() {
        return String.format(".word\t%d", value_);
    }
}
