package backend;

import java.util.*;

import IR.inst.IRInst;
import IR.module.*;
import IR.value.var.IRLocalVar;

public class LinearScanRegAllocator {
    private HashMap<IRInst, Integer> linearOrderMap_;
    private HashMap<IRInst, HashSet<IRLocalVar>> useMap_, defMap_, inMap_, outMap_;
    private HashMap<IRInst, ArrayList<IRInst>> succMap_, predMap_;

    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    public void visit(IRFuncDef node) {
        getLinearOrder(node);

    }

    private void getLinearOrder(IRFuncDef node) {
        linearOrderMap_ = new HashMap<>();
        ArrayList<IRBasicBlock> rpo = node.getRPO();
        int instId = 0;
        for (var block : rpo) {
            for (var inst : block.instList_) {
                linearOrderMap_.put(inst, instId++);
            }
        }
    }
}
