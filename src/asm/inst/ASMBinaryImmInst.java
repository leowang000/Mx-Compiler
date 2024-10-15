package asm.inst;

import asm.util.Register;

public class ASMBinaryImmInst extends ASMInst {
    public String op_;
    public Register rd_, rs_;
    public int imm_;

    public ASMBinaryImmInst(String op, Register rd, Register rs, int imm) {
        op_ = op;
        rd_ = rd;
        rs_ = rs;
        imm_ = imm;
    }

    public ASMBinaryImmInst(String op, String rd, String rs, int imm) {
        op_ = op;
        rd_ = new Register(rd);
        rs_ = new Register(rs);
        imm_ = imm;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s, %s, %d", op_, rd_, rs_, imm_);
    }
}
