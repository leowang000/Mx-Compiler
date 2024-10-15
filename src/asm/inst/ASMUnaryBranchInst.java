package asm.inst;

import asm.util.Register;

public class ASMUnaryBranchInst extends ASMInst {
    public String op_, dest_;
    public Register rs_;

    public ASMUnaryBranchInst(String op, Register rs, String dest) {
        op_ = op;
        rs_ = rs;
        dest_ = dest;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s, %s", op_, rs_, dest_);
    }
}
