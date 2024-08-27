package asm.module;

import java.util.ArrayList;

import asm.ASMNode;
import asm.inst.ASMInst;

public class ASMBlock extends ASMNode {
    public String label_, info_;
    public ArrayList<ASMInst> instList_;

    public ASMBlock(String label) {
        label_ = label;
        instList_ = new ArrayList<>();
        info_ = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (info_ != null) {
            sb.append(info_).append("\n");
        }
        sb.append(label_).append(":\n");
        for (var inst : instList_) {
            sb.append("\t").append(inst).append("\n");
        }
        return sb.toString();
    }
}
