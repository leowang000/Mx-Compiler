package asm.inst;

import asm.util.MemAddr;
import asm.util.Register;

public class ASMLwInst extends ASMInst {
    public Register rd_;
    public MemAddr addr_;

    public ASMLwInst(Register rd, Register base, int offset) {
        rd_ = rd;
        addr_ = new MemAddr(base, offset);
    }

    public ASMLwInst(String rd, String base, int offset) {
        rd_ = new Register(rd);
        addr_ = new MemAddr(base, offset);
    }

    @Override
    public String toString() {
        return String.format("lw\t%s, %s", rd_, addr_);
    }
}
