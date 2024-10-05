package asm.module;

import java.util.ArrayList;

import asm.ASMNode;

public class ASMSection extends ASMNode {
    public String name_;
    public ArrayList<ASMBlock> blockList_ = new ArrayList<>();

    public ASMSection(String name) {
        name_ = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".section ").append(name_).append("\n");
        for (var block : blockList_) {
            sb.append(block);
        }
        return sb.toString();
    }
}
