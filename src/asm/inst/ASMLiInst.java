package asm.inst;

import asm.util.Register;

public class ASMLiInst extends ASMInst {
    public Register rd_;
    public int imm_;

    public ASMLiInst(Register rd, int imm) {
        rd_ = rd;
        imm_ = imm;
    }

    public ASMLiInst(String rd, int imm) {
        rd_ = new Register(rd);
        imm_ = imm;
    }

    @Override
    public String toString() {
        return String.format("li\t%s, %s", rd_, imm_);
    }
}
