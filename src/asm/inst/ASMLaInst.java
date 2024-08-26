package asm.inst;

import asm.util.Register;

public class ASMLaInst extends ASMInst {
    public Register rd_;
    public String label_;

    public ASMLaInst(String rd, String label) {
        rd_ = new Register(rd);
        label_ = label;
    }

    @Override
    public String toString() {
        return String.format("la\t%s, %s", rd_, label_);
    }
}
