package asm.inst;

import asm.util.Register;

public class ASMUnaryInst extends ASMInst {
    public String op_;
    public Register rd_, rs_;

    public ASMUnaryInst(String op, Register rd, Register rs) {
        op_ = op;
        rd_ = rd;
        rs_ = rs;
    }

    public ASMUnaryInst(String op, String rd, String rs) {
        op_ = op;
        rd_ = new Register(rd);
        rs_ = new Register(rs);
    }

    @Override
    public String toString() {
        return String.format("%s\t%s, %s", op_, rd_, rs_);
    }
}
