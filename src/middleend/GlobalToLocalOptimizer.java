package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.IRFuncDef;
import IR.module.IRProgram;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRGlobalVar;
import IR.value.var.IRLocalVar;

public class GlobalToLocalOptimizer {
    private IRProgram irProgram_;
    private final HashMap<IRFuncDef, Node> funcNodeMap_ = new HashMap<>();
    private final HashMap<ShrunkNode, HashSet<IRGlobalVar>> changedVarMap_ = new HashMap<>();
    private final HashMap<IRFuncDef, HashSet<IRGlobalVar>> usedVarMap_ = new HashMap<>();
    private static int globalConstantCnt_ = 0;

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
        public ShrunkNode pred_ = null;
        public HashSet<ShrunkNode> succSet_ = new HashSet<>();
    }

    public void visit(IRProgram node) {
        irProgram_ = node;
        buildCallGraph();
        kosaraju();
        for (var funcDef : node.funcDefMap_.values()) {
            getUsedGlobalVars(funcDef);
        }
        getChangedGlobalVars(funcNodeMap_.get(irProgram_.funcDefMap_.get("main")).belong_);
        for (var funcDef : node.funcDefMap_.values()) {
            globalConstantOptimize(funcDef);
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
                    succ.belong_.pred_ = node.belong_;
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

    private void getUsedGlobalVars(IRFuncDef funcDef) {
        HashSet<IRGlobalVar> usedGlobalVarSet = new HashSet<>();
        for (var block : funcDef.body_) {
            for (var inst : block.instList_) {
                if (inst instanceof IRLoadInst) {
                    IRLoadInst loadInst = (IRLoadInst) inst;
                    if (loadInst.pointer_ instanceof IRGlobalVar) {
                        usedGlobalVarSet.add((IRGlobalVar) loadInst.pointer_);
                    }
                }
            }
        }
        usedVarMap_.put(funcDef, usedGlobalVarSet);
    }

    private HashSet<IRGlobalVar> getChangedGlobalVars(ShrunkNode shrunkNode) {
        HashSet<IRGlobalVar> changedGlobalVarSet = new HashSet<>();
        for (var succ : shrunkNode.succSet_) {
            changedGlobalVarSet.addAll(getChangedGlobalVars(succ));
        }
        for (var node : shrunkNode.nodeSet_) {
            for (var block : node.funcDef_.body_) {
                for (var inst : block.instList_) {
                    if (inst instanceof IRStoreInst) {
                        IRStoreInst storeInst = (IRStoreInst) inst;
                        if (storeInst.pointer_ instanceof IRGlobalVar) {
                            changedGlobalVarSet.add((IRGlobalVar) storeInst.pointer_);
                        }
                    }
                }
            }
        }
        changedVarMap_.put(shrunkNode, changedGlobalVarSet);
        return changedGlobalVarSet;
    }

    private void globalConstantOptimize(IRFuncDef funcDef) {
        HashSet<IRGlobalVar> changedGlobalVarSet = changedVarMap_.get(funcNodeMap_.get(funcDef).belong_);
        ArrayList<IRLoadInst> loadConstantInstList = new ArrayList<>();
        HashMap<IRGlobalVar, IRLocalVar> globalVarValueMap = new HashMap<>();
        HashMap<IRLocalVar, IRLocalVar> substitutionMap = new HashMap<>();
        for (var usedGlobalVar : usedVarMap_.get(funcDef)) {
            if (changedGlobalVarSet.contains(usedGlobalVar)) {
                continue;
            }
            IRLocalVar localVar =
                new IRLocalVar(String.format("global_constant.%s.%d", usedGlobalVar.name_, globalConstantCnt_++),
                               ((IRPtrType) usedGlobalVar.type_).getDereferenceType());
            loadConstantInstList.add(new IRLoadInst(localVar, usedGlobalVar));
            globalVarValueMap.put(usedGlobalVar, localVar);
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
}
