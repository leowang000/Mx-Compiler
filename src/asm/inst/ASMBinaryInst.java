package asm.inst;

import asm.util.Register;

public class ASMBinaryInst extends ASMInst {
    public String op_;
    public Register rd_, rs1_, rs2_;

    public ASMBinaryInst(String op, Register rd, Register rs1, Register rs2) {
        op_ = op;
        rd_ = rd;
        rs1_ = rs1;
        rs2_ = rs2;
    }

    public ASMBinaryInst(String op, String rd, String rs1, String rs2) {
        op_ = op;
        rd_ = new Register(rd);
        rs1_ = new Register(rs1);
        rs2_ = new Register(rs2);
    }

    @Override
    public String toString() {
        return String.format("%s\t%s, %s, %s", op_, rd_, rs1_, rs2_);
    }
}
