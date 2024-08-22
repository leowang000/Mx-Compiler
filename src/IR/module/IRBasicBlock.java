package IR.module;

import java.util.ArrayList;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.IRInst;
import IR.value.var.IRLocalVar;

public class IRBasicBlock extends IRNode {
    public String label_;
    public ArrayList<IRInst> instList_;
    public IRFuncDef belong_;
    public IRLocalVar result_;

    public IRBasicBlock(String label, IRFuncDef belong) {
        label_ = label;
        instList_ = new ArrayList<>();
        belong_ = belong;
        result_ = null;
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
}
