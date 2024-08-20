package IR.module;

import java.util.ArrayList;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.IRInst;

public class IRBasicBlock extends IRNode {
    public String label_;
    public ArrayList<IRInst> instList_;

    public IRBasicBlock(String label) {
        label_ = label;
        instList_ = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label_).append("\n");
        for (var inst : instList_) {
            sb.append("\t").append(inst).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
