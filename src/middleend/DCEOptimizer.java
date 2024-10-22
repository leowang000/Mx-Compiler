package middleend;

import java.util.*;

import IR.inst.IRCallInst;
import IR.inst.IRInst;
import IR.module.IRFuncDef;
import IR.module.IRProgram;
import IR.value.var.IRLocalVar;

public class DCEOptimizer {
    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    public void visit(IRFuncDef node) {
        HashSet<IRLocalVar> workList = new HashSet<>();
        HashMap<IRLocalVar, IRInst> defMap = new HashMap<>();
        HashMap<IRLocalVar, HashSet<IRInst>> useMap = new HashMap<>();
        HashMap<IRInst, Boolean> shouldRemove = new HashMap<>();
        HashSet<IRLocalVar> funcArgs = new HashSet<>(node.args_);
        for (var block : node.body_) {
            for (var inst : block.phiMap_.values()) {
                initializeInst(inst, workList, defMap, useMap, shouldRemove, funcArgs);
            }
            for (var inst : block.instList_) {
                initializeInst(inst, workList, defMap, useMap, shouldRemove, funcArgs);
            }
        }
        while (!workList.isEmpty()) {
            Iterator<IRLocalVar> iter = workList.iterator();
            IRLocalVar localVar = iter.next();
            iter.remove();
            if (!useMap.get(localVar).isEmpty()) {
                continue;
            }
            IRInst def = defMap.get(localVar);
            if (def instanceof IRCallInst) {
                continue;
            }
            shouldRemove.put(def, true);
            for (var usedVar : def.use_) {
                if (!funcArgs.contains(usedVar)) {
                    useMap.get(usedVar).remove(def);
                }
            }
        }
        for (var block : node.body_) {
            block.phiMap_.entrySet().removeIf(entry -> shouldRemove.get(entry.getValue()));
            block.instList_.removeIf(shouldRemove::get);
        }
    }

    private void initializeInst(IRInst inst, HashSet<IRLocalVar> workList, HashMap<IRLocalVar, IRInst> defMap,
                                HashMap<IRLocalVar, HashSet<IRInst>> useMap, HashMap<IRInst, Boolean> shouldRemove,
                                HashSet<IRLocalVar> funcArgs) {
        inst.getUseAndDef();
        workList.addAll(inst.def_);
        for (var localVar : inst.def_) {
            defMap.put(localVar, inst);
        }
        for (var usedVar : inst.use_) {
            if (!funcArgs.contains(usedVar)) {
                if (!useMap.containsKey(usedVar)) {
                    useMap.put(usedVar, new HashSet<>(List.of(inst)));
                }
                else {
                    useMap.get(usedVar).add(inst);
                }
            }
        }
        shouldRemove.put(inst, false);
    }
}
