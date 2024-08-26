package asm.inst;

import asm.util.MemAddr;
import asm.util.Register;

public class ASMSwInst extends ASMInst {
    public Register rs_;
    public MemAddr addr_;

    public ASMSwInst(String rs, String base, int offset) {
        rs_ = new Register(rs);
        addr_ = new MemAddr(base, offset);
    }

    public ASMSwInst(String rs, MemAddr addr) {
        rs_ = new Register(rs);
        addr_ = addr;
    }

    @Override
    public String toString() {
        return String.format("sw\t%s, %s", rs_, addr_);
    }
}
