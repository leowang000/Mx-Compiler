package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRGlobalVar;
import IR.value.var.IRLocalVar;

public class GlobalToLocalOptimizer {
    private IRProgram irProgram_;
    private final HashMap<IRFuncDef, Node> funcNodeMap_ = new HashMap<>();
    private final HashMap<ShrunkNode, HashSet<IRGlobalVar>> changedVarMap_ = new HashMap<>();
    private final HashMap<ShrunkNode, HashSet<IRGlobalVar>> appearingVarMap_ = new HashMap<>();
    private final HashMap<IRFuncDef, HashSet<IRGlobalVar>> loadedVarMap_ = new HashMap<>();
    private ArrayList<ShrunkNode> topo_ = null;
    private static int globalConstantCnt_ = 0, globalVarCnt_ = 0, tmpVarCnt_ = 0;

    private static class Node {
        public IRFuncDef funcDef_;
        public HashSet<Node> succSet_ = new HashSet<>(), predSet_ = new HashSet<>();
        public ShrunkNode belong_ = null;

        public Node(IRFuncDef funcDef) {
            funcDef_ = funcDef;
        }
    }

    private static class ShrunkNode {
        public HashSet<Node> nodeSet_ = new HashSet<>();
        public HashSet<ShrunkNode> succSet_ = new HashSet<>(), predSet_ = new HashSet<>();
    }

    public void visit(IRProgram node) {
        irProgram_ = node;
        buildCallGraph();
        kosaraju();
        for (var funcDef : node.funcDefMap_.values()) {
            getLoadedGlobalVars(funcDef);
        }
        getTopo();
        getChangedAndAppearingGlobalVars();
        for (var funcDef : node.funcDefMap_.values()) {
            globalConstantOptimize(funcDef);
        }
        for (var funcDef : node.funcDefMap_.values()) {
            globalVariableOptimize(funcDef);
        }
    }

    private void buildCallGraph() {
        for (var funcDef : irProgram_.funcDefMap_.values()) {
            funcNodeMap_.put(funcDef, new Node(funcDef));
        }
        for (var funcDef : irProgram_.funcDefMap_.values()) {
            for (var block : funcDef.body_) {
                for (var inst : block.instList_) {
                    if (inst instanceof IRCallInst) {
                        IRCallInst callInst = (IRCallInst) inst;
                        IRFuncDef callee = irProgram_.funcDefMap_.get(callInst.funcName_);
                        if (callee != null) {
                            funcNodeMap_.get(funcDef).succSet_.add(funcNodeMap_.get(callee));
                            funcNodeMap_.get(callee).predSet_.add(funcNodeMap_.get(funcDef));
                        }
                    }
                }
            }
        }
    }

    private void kosaraju() {
        ArrayList<Node> order = new ArrayList<>(funcNodeMap_.values());
        HashSet<Node> visited = new HashSet<>();
        for (var node : funcNodeMap_.values()) {
            dfsSucc(node, visited, order);
        }
        visited.clear();
        for (int i = order.size() - 1; i >= 0; i--) {
            if (!visited.contains(order.get(i))) {
                ShrunkNode shrunkNode = new ShrunkNode();
                dfsPred(order.get(i), visited, shrunkNode);
            }
        }
        for (var node : funcNodeMap_.values()) {
            for (var succ : node.succSet_) {
                if (node.belong_ != succ.belong_) {
                    node.belong_.succSet_.add(succ.belong_);
                    succ.belong_.predSet_.add(node.belong_);
                }
            }
        }
    }

    private void dfsSucc(Node node, HashSet<Node> visited, ArrayList<Node> order) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        for (var succ : node.succSet_) {
            dfsSucc(succ, visited, order);
        }
        order.add(node);
    }

    private void dfsPred(Node node, HashSet<Node> visited, ShrunkNode shrunkNode) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        node.belong_ = shrunkNode;
        shrunkNode.nodeSet_.add(node);
        for (var pred : node.predSet_) {
            dfsPred(pred, visited, shrunkNode);
        }
    }

    private void getLoadedGlobalVars(IRFuncDef funcDef) {
        HashSet<IRGlobalVar> loadedGlobalVarSet = new HashSet<>();
        for (var block : funcDef.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRLoadInst) {
                    IRLoadInst loadInst = (IRLoadInst) inst;
                    if (loadInst.pointer_ instanceof IRGlobalVar) {
                        loadedGlobalVarSet.add((IRGlobalVar) loadInst.pointer_);
                    }
                }
            }
        }
        loadedVarMap_.put(funcDef, loadedGlobalVarSet);
    }

    private void getTopo() {
        topo_ = new ArrayList<>();
        HashMap<ShrunkNode, Integer> in = new HashMap<>();
        HashSet<ShrunkNode> srcSet = new HashSet<>();
        for (var funcDef : irProgram_.funcDefMap_.values()) {
            ShrunkNode shrunkNode = funcNodeMap_.get(funcDef).belong_;
            in.put(shrunkNode, shrunkNode.predSet_.size());
            if (shrunkNode.predSet_.isEmpty()) {
                srcSet.add(shrunkNode);
            }
        }
        while (!srcSet.isEmpty()) {
            ShrunkNode shrunkNode = srcSet.iterator().next();
            srcSet.remove(shrunkNode);
            topo_.add(shrunkNode);
            for (var succ : shrunkNode.succSet_) {
                in.put(succ, in.get(succ) - 1);
                if (in.get(succ) == 0) {
                    srcSet.add(succ);
                }
            }
        }
    }

    private void getChangedAndAppearingGlobalVars() {
        for (int i = topo_.size() - 1; i >= 0; i--) {
            ShrunkNode shrunkNode = topo_.get(i);
            HashSet<IRGlobalVar> changedGlobalVarSet = new HashSet<>();
            HashSet<IRGlobalVar> appearingGlobalVarSet = new HashSet<>();
            for (var succ : shrunkNode.succSet_) {
                changedGlobalVarSet.addAll(changedVarMap_.get(succ));
                appearingGlobalVarSet.addAll(appearingVarMap_.get(succ));
            }
            for (var node : shrunkNode.nodeSet_) {
                for (var block : node.funcDef_.body_) {
                    for (var inst : block.instList_) {
                        if (inst instanceof IRStoreInst) {
                            IRStoreInst storeInst = (IRStoreInst) inst;
                            if (storeInst.pointer_ instanceof IRGlobalVar) {
                                changedGlobalVarSet.add((IRGlobalVar) storeInst.pointer_);
                                appearingGlobalVarSet.add((IRGlobalVar) storeInst.pointer_);
                            }
                        }
                        if (inst instanceof IRLoadInst) {
                            IRLoadInst loadInst = (IRLoadInst) inst;
                            if (loadInst.pointer_ instanceof IRGlobalVar) {
                                appearingGlobalVarSet.add((IRGlobalVar) loadInst.pointer_);
                            }
                        }
                    }
                }
            }
            changedVarMap_.put(shrunkNode, changedGlobalVarSet);
            appearingVarMap_.put(shrunkNode, appearingGlobalVarSet);
        }
    }

    private void globalConstantOptimize(IRFuncDef funcDef) {
        HashSet<IRGlobalVar> changedGlobalVarSet = changedVarMap_.get(funcNodeMap_.get(funcDef).belong_);
        ArrayList<IRLoadInst> loadConstantInstList = new ArrayList<>();
        HashMap<IRGlobalVar, IRLocalVar> globalVarValueMap = new HashMap<>();
        HashMap<IRLocalVar, IRLocalVar> substitutionMap = new HashMap<>();
        for (var loadedGlobalVar : loadedVarMap_.get(funcDef)) {
            if (changedGlobalVarSet.contains(loadedGlobalVar)) {
                continue;
            }
            IRLocalVar localVar =
                new IRLocalVar(String.format("global_constant.%s.%d", loadedGlobalVar.name_, globalConstantCnt_++),
                               ((IRPtrType) loadedGlobalVar.type_).getDereferenceType());
            loadConstantInstList.add(new IRLoadInst(localVar, loadedGlobalVar));
            globalVarValueMap.put(loadedGlobalVar, localVar);
        }
        for (var block : funcDef.body_) {
            ArrayList<IRInst> newInstList = new ArrayList<>();
            for (var inst : block.instList_) {
                if (inst instanceof IRLoadInst) {
                    IRLoadInst loadInst = (IRLoadInst) inst;
                    if ((loadInst.pointer_ instanceof IRGlobalVar) &&
                        globalVarValueMap.containsKey(loadInst.pointer_)) {
                        substitutionMap.put(loadInst.result_, globalVarValueMap.get(loadInst.pointer_));
                        continue;
                    }
                }
                newInstList.add(getSubstitution(inst, substitutionMap));
            }
            block.instList_ = newInstList;
        }
        funcDef.body_.get(0).instList_.addAll(0, loadConstantInstList);
    }

    private IRInst getSubstitution(IRInst inst, HashMap<IRLocalVar, IRLocalVar> substitutionMap) {
        if (inst instanceof IRAllocaInst) {
            return inst;
        }
        if (inst instanceof IRBinaryInst) {
            IRBinaryInst binaryInst = (IRBinaryInst) inst;
            binaryInst.lhs_ = getSubstitution(binaryInst.lhs_, substitutionMap);
            binaryInst.rhs_ = getSubstitution(binaryInst.rhs_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRBrInst) {
            IRBrInst brInst = (IRBrInst) inst;
            brInst.cond_ = getSubstitution(brInst.cond_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRCallInst) {
            IRCallInst callInst = (IRCallInst) inst;
            callInst.args_.replaceAll(arg -> getSubstitution(arg, substitutionMap));
            return inst;
        }
        if (inst instanceof IRGetElementPtrInst) {
            IRGetElementPtrInst getElementPtrInst = (IRGetElementPtrInst) inst;
            getElementPtrInst.ptr_ = getSubstitution(getElementPtrInst.ptr_, substitutionMap);
            getElementPtrInst.id1_ = getSubstitution(getElementPtrInst.id1_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRIcmpInst) {
            IRIcmpInst icmpInst = (IRIcmpInst) inst;
            icmpInst.lhs_ = getSubstitution(icmpInst.lhs_, substitutionMap);
            icmpInst.rhs_ = getSubstitution(icmpInst.rhs_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRJumpInst) {
            return inst;
        }
        if (inst instanceof IRLoadInst) {
            IRLoadInst loadInst = (IRLoadInst) inst;
            loadInst.pointer_ = getSubstitution(loadInst.pointer_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRMoveInst) {
            return inst;
        }
        if (inst instanceof IRPhiInst) {
            return inst;
        }
        if (inst instanceof IRRetInst) {
            IRRetInst retInst = (IRRetInst) inst;
            retInst.value_ = getSubstitution(retInst.value_, substitutionMap);
            return inst;
        }
        if (inst instanceof IRStoreInst) {
            IRStoreInst storeInst = (IRStoreInst) inst;
            storeInst.pointer_ = getSubstitution(storeInst.pointer_, substitutionMap);
            storeInst.value_ = getSubstitution(storeInst.value_, substitutionMap);
            return inst;
        }
        return null;
    }

    private IRValue getSubstitution(IRValue value, HashMap<IRLocalVar, IRLocalVar> substitutionMap) {
        if (value instanceof IRLocalVar) {
            IRLocalVar localVar = (IRLocalVar) value;
            if (substitutionMap.containsKey(localVar)) {
                return substitutionMap.get(localVar);
            }
        }
        return value;
    }

    public void globalVariableOptimize(IRFuncDef funcDef) {
        if (funcNodeMap_.get(funcDef).belong_.nodeSet_.size() > 1) {
            return;
        }
        HashSet<IRGlobalVar> storedGlobalVarSet = new HashSet<>();
        for (var block : funcDef.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRStoreInst) {
                    IRStoreInst storeInst = (IRStoreInst) inst;
                    if (storeInst.pointer_ instanceof IRGlobalVar) {
                        storedGlobalVarSet.add((IRGlobalVar) storeInst.pointer_);
                    }
                }
            }
        }
        for (var succ : funcNodeMap_.get(funcDef).belong_.succSet_) {
            storedGlobalVarSet.removeAll(appearingVarMap_.get(succ));
        }
        storedGlobalVarSet.removeIf(globalVar -> !loadedVarMap_.get(funcDef).contains(globalVar));
        HashMap<IRBasicBlock, ArrayList<IRInst>> returnBLockMap = new HashMap<>();
        for (var block : funcDef.body_) {
            if (block.instList_.get(block.instList_.size() - 1) instanceof IRRetInst) {
                returnBLockMap.put(block, new ArrayList<>());
            }
        }
        ArrayList<IRInst> entryTmpInstList = new ArrayList<>();
        HashMap<IRGlobalVar, IRLocalVar> substitutionMap = new HashMap<>();
        for (var globalVar : storedGlobalVarSet) {
            IRLocalVar newVar =
                new IRLocalVar(String.format("global.%s.%d", globalVar.name_, globalVarCnt_++), globalVar.type_);
            IRLocalVar entryTmpVar =
                new IRLocalVar(String.format("global_tmp.%s.%d", globalVar.name_, tmpVarCnt_++), globalVar.type_);
            entryTmpInstList.add(new IRAllocaInst(newVar));
            entryTmpInstList.add(new IRLoadInst(entryTmpVar, globalVar));
            entryTmpInstList.add(new IRStoreInst(entryTmpVar, newVar));
            for (var returnTmpInstList : returnBLockMap.values()) {
                IRLocalVar returnTmpVar =
                    new IRLocalVar(String.format("global_tmp.%s.%d", globalVar.name_, tmpVarCnt_++), globalVar.type_);
                returnTmpInstList.add(new IRLoadInst(returnTmpVar, newVar));
                returnTmpInstList.add(new IRStoreInst(returnTmpVar, globalVar));
            }
            substitutionMap.put(globalVar, newVar);
        }
        for (var block : funcDef.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRStoreInst) {
                    IRStoreInst storeInst = (IRStoreInst) inst;
                    if (storeInst.pointer_ instanceof IRGlobalVar) {
                        IRGlobalVar globalVar = (IRGlobalVar) storeInst.pointer_;
                        if (substitutionMap.containsKey(globalVar)) {
                            storeInst.pointer_ = substitutionMap.get(globalVar);
                        }
                    }
                }
                if (inst instanceof IRLoadInst) {
                    IRLoadInst loadInst = (IRLoadInst) inst;
                    if (loadInst.pointer_ instanceof IRGlobalVar) {
                        IRGlobalVar globalVar = (IRGlobalVar) loadInst.pointer_;
                        if (substitutionMap.containsKey(globalVar)) {
                            loadInst.pointer_ = substitutionMap.get(globalVar);
                        }
                    }
                }
            }
        }
        funcDef.body_.get(0).instList_.addAll(0, entryTmpInstList);
        for (var entry : returnBLockMap.entrySet()) {
            entry.getKey().instList_.addAll(entry.getKey().instList_.size() - 1, entry.getValue());
        }
    }
}