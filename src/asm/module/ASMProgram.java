package asm.module;

import asm.ASMNode;

public class ASMProgram extends ASMNode {
    public ASMSection text_, data_, rodata_;

    public ASMProgram() {
        text_ = new ASMSection("text");
        data_ = new ASMSection("data");
        rodata_ = new ASMSection("rodata");
    }

    @Override
    public String toString() {
        return text_ + "\n" + data_ + "\n" + rodata_;
    }
}
