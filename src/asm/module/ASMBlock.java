package asm.module;

import java.util.ArrayList;

import asm.ASMNode;
import asm.inst.ASMInst;

public class ASMBlock extends ASMNode {
    public String label_;
    public ArrayList<ASMInst> instList_;

    public ASMBlock(String label) {
        label_ = label;
        instList_ = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label_).append(":\n");
        for (var inst : instList_) {
            sb.append("\t").append(inst).append("\n");
        }
        return sb.toString();
    }
}
