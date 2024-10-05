package IR.module;

import java.util.ArrayList;
import java.util.HashSet;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.IRInst;

public class IRBasicBlock extends IRNode {
    public String label_;
    public ArrayList<IRInst> instList_ = new ArrayList<>();
    public IRFuncDef belong_;
    public HashSet<IRBasicBlock> preds_ = new HashSet<>(), succs_ = new HashSet<>(); // CFG
    public IRBasicBlock idom_ = null;
    public ArrayList<IRBasicBlock> domChildren_ = new ArrayList<>();
    public HashSet<IRBasicBlock> domBoundary_ = new HashSet<>();

    public IRBasicBlock(String label, IRFuncDef belong) {
        label_ = label;
        belong_ = belong;
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

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    public String getLabel() {
        return "%" + label_;
    }
}
