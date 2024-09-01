package asm.inst;

import asm.util.Register;

public class ASMBranchInst extends ASMInst {
    public String op_, dest_;
    public Register rs1_, rs2_;

    public ASMBranchInst(String op, String rs1, String rs2, String dest) {
        op_ = op;
        rs1_ = new Register(rs1);
        rs2_ = new Register(rs2);
        dest_ = dest;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s, %s, %s", op_, rs1_, rs2_, dest_);
    }
}
