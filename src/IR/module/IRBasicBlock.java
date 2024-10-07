package IR.module;

import java.util.*;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.*;
import IR.value.var.IRLocalVar;

public class IRBasicBlock extends IRNode {
    public String label_;
    public HashMap<IRLocalVar, IRPhiInst> phiMap_ = new HashMap<>();
    public ArrayList<IRInst> instList_ = new ArrayList<>();
    public ArrayList<IRMoveInst> moveList_ = new ArrayList<>();
    public IRFuncDef belong_;
    public ArrayList<IRBasicBlock> preds_ = new ArrayList<>(); // CFG
    public HashSet<IRBasicBlock> succs_ = new HashSet<>(); // CFG
    public IRBasicBlock idom_ = null; // domTree
    public ArrayList<IRBasicBlock> domChildren_ = new ArrayList<>(), domFrontiers_ = new ArrayList<>(); // domTree

    public IRBasicBlock(String label, IRFuncDef belong) {
        label_ = label;
        belong_ = belong;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label_).append(":\n");
        for (var phiInst : phiMap_.values()) {
            sb.append("\t").append(phiInst).append("\n");
        }
        for (var inst : instList_) {
            sb.append("\t").append(inst).append("\n");
        }
        if (!moveList_.isEmpty()) {
            sb.append("\t{\n");
            for (var inst : moveList_) {
                sb.append("\t\t").append(inst).append("\n");
            }
            sb.append("\t}\n");
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
