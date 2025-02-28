package middleend;

import java.util.*;

import IR.inst.IRCallInst;
import IR.inst.IRInst;
import IR.module.IRFuncDef;
import IR.module.IRProgram;
import IR.value.var.IRLocalVar;

public class DCEOptimizer {
    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        HashSet<IRLocalVar> workList = new HashSet<>();
        HashMap<IRLocalVar, IRInst> defInstMap = new HashMap<>();
        HashMap<IRLocalVar, HashSet<IRInst>> useMap = new HashMap<>();
        HashMap<IRInst, Boolean> shouldRemove = new HashMap<>();
        HashSet<IRLocalVar> funcArgs = new HashSet<>(node.args_);
        for (var block : node.body_) {
            for (var inst : block.phiMap_.values()) {
                initialize(inst, workList, defInstMap, useMap, shouldRemove, funcArgs);
            }
            for (var inst : block.instList_) {
                initialize(inst, workList, defInstMap, useMap, shouldRemove, funcArgs);
            }
        }
        while (!workList.isEmpty()) {
            Iterator<IRLocalVar> iter = workList.iterator();
            IRLocalVar localVar = iter.next();
            iter.remove();
            if (useMap.containsKey(localVar) && !useMap.get(localVar).isEmpty()) {
                continue;
            }
            IRInst defInst = defInstMap.get(localVar);
            if (defInst instanceof IRCallInst) {
                continue;
            }
            shouldRemove.put(defInst, true);
            for (var usedVar : defInst.use_) {
                if (!funcArgs.contains(usedVar)) {
                    useMap.get(usedVar).remove(defInst);
                }
            }
        }
        for (var block : node.body_) {
            block.phiMap_.entrySet().removeIf(entry -> shouldRemove.get(entry.getValue()));
            block.instList_.removeIf(shouldRemove::get);
        }
    }

    private void initialize(IRInst inst, HashSet<IRLocalVar> workList, HashMap<IRLocalVar, IRInst> defMap,
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
