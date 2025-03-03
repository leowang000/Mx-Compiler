package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.type.IRIntType;
import IR.value.IRValue;
import IR.value.constant.*;
import IR.value.var.IRGlobalVar;
import IR.value.var.IRLocalVar;

// Must be used after alloca elimination.
public class SCCPOptimizer {
    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
        for (var funcDef : node.funcDefMap_.values()) {
            for (var block : funcDef.body_) {
                IRInst lastInst = block.instList_.get(block.instList_.size() - 1);
                if (lastInst instanceof IRBrInst) {
                    IRBrInst brInst = (IRBrInst) lastInst;
                    if (brInst.cond_ instanceof IRBoolConst) {
                        boolean cond = ((IRBoolConst) brInst.cond_).value_;
                        IRBasicBlock reachableBlock = (cond ? brInst.trueBlock_ : brInst.falseBlock_);
                        IRBasicBlock unreachableBlock = (cond ? brInst.falseBlock_ : brInst.trueBlock_);
                        block.instList_.set(block.instList_.size() - 1, new IRJumpInst(reachableBlock));
                        for (var phiInst : unreachableBlock.phiMap_.values()) {
                            phiInst.info_.remove(block);
                        }
                    }
                }
            }
        }
        new CFGBuilder().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            HashSet<IRBasicBlock> reachable = new HashSet<>();
            HashSet<IRBasicBlock> workList = new HashSet<>(List.of(funcDef.body_.get(0)));
            while (!workList.isEmpty()) {
                IRBasicBlock block = workList.iterator().next();
                workList.remove(block);
                if (reachable.contains(block)) {
                    continue;
                }
                reachable.add(block);
                workList.addAll(block.succs_);
            }
            funcDef.body_.removeIf(block -> !reachable.contains(block));
            for (var block : funcDef.body_) {
                for (var phiInst : block.phiMap_.values()) {
                    phiInst.info_.entrySet().removeIf(entry -> !reachable.contains(entry.getKey()));
                }
            }
        }
    }

    private static class Edge {
        IRBasicBlock from_, to_;

        public Edge(IRBasicBlock from, IRBasicBlock to) {
            from_ = from;
            to_ = to;
        }
    }

    private enum Status {
        UNKNOWN, CONST, NON_CONST
    }

    private void visit(IRFuncDef node) {
        Queue<Object> workList = new LinkedList<>(List.of(new Edge(new IRBasicBlock("", null), node.body_.get(0))));
        HashMap<IRBasicBlock, HashSet<IRBasicBlock>> executableMap = new HashMap<>();
        HashMap<IRLocalVar, ArrayList<IRInst>> useInstMap = new HashMap<>();
        HashMap<IRLocalVar, Status> statusMap = new HashMap<>();
        HashMap<IRLocalVar, Integer> constValueMap = new HashMap<>();
        HashMap<IRInst, IRBasicBlock> belongMap = new HashMap<>();
        for (var arg : node.args_) {
            statusMap.put(arg, Status.NON_CONST);
        }
        for (var block : node.body_) {
            executableMap.put(block, new HashSet<>());
            for (var inst : block.phiMap_.values()) {
                initialize(inst, block, useInstMap, statusMap, belongMap);
            }
            for (var inst : block.instList_) {
                initialize(inst, block, useInstMap, statusMap, belongMap);
            }
        }
        while (!workList.isEmpty()) {
            Object item = workList.poll();
            if (item instanceof IRInst) {
                IRInst inst = (IRInst) item;
                visitInst(inst, belongMap.get(inst), workList, useInstMap, statusMap, constValueMap, belongMap,
                          executableMap);
                continue;
            }
            Edge edge = (Edge) item;
            HashSet<IRBasicBlock> executableSet = executableMap.get(edge.to_);
            if (executableSet.contains(edge.from_)) {
                continue;
            }
            executableSet.add(edge.from_);
            for (var phiInst : edge.to_.phiMap_.values()) {
                visitInst(phiInst, belongMap.get(phiInst), workList, useInstMap, statusMap, constValueMap,
                          belongMap, executableMap);
            }
            if (executableSet.size() == 1) {
                for (var inst : edge.to_.instList_) {
                    visitInst(inst, belongMap.get(inst), workList, useInstMap, statusMap, constValueMap, belongMap,
                              executableMap);
                }
            }
        }
        for (var block : node.body_) {
            block.phiMap_.entrySet().removeIf(entry -> constValueMap.containsKey(entry.getValue().result_));
            block.instList_.removeIf(
                entry -> !entry.def_.isEmpty() && constValueMap.containsKey(entry.def_.iterator().next()));
            for (var phiInst : block.phiMap_.values()) {
                substitute(phiInst, constValueMap);
            }
            for (var inst : block.instList_) {
                substitute(inst, constValueMap);
            }
        }
    }

    private void initialize(IRInst inst, IRBasicBlock block, HashMap<IRLocalVar, ArrayList<IRInst>> useInstMap,
                            HashMap<IRLocalVar, Status> statusMap, HashMap<IRInst, IRBasicBlock> belongMap) {
        inst.getUseAndDef();
        for (var use : inst.use_) {
            if (useInstMap.containsKey(use)) {
                useInstMap.get(use).add(inst);
            }
            else {
                useInstMap.put(use, new ArrayList<>(List.of(inst)));
            }
        }
        for (var def : inst.def_) {
            statusMap.put(def, Status.UNKNOWN);
            if (!useInstMap.containsKey(def)) {
                useInstMap.put(def, new ArrayList<>());
            }
        }
        belongMap.put(inst, block);
    }

    private void visitInst(IRInst inst, IRBasicBlock belong, Queue<Object> workList,
                           HashMap<IRLocalVar, ArrayList<IRInst>> useInstMap, HashMap<IRLocalVar, Status> statusMap,
                           HashMap<IRLocalVar, Integer> constValueMap, HashMap<IRInst, IRBasicBlock> belongMap,
                           HashMap<IRBasicBlock, HashSet<IRBasicBlock>> executableMap) {
        if ((inst instanceof IRRetInst) || (inst instanceof IRStoreInst) ||
            (inst instanceof IRCallInst && ((IRCallInst) inst).result_ == null)) {
            return;
        }
        if (inst instanceof IRJumpInst) {
            IRJumpInst jumpInst = (IRJumpInst) inst;
            workList.offer(new Edge(belongMap.get(inst), jumpInst.destBlock_));
            return;
        }
        if (inst instanceof IRBrInst) {
            IRBrInst brInst = (IRBrInst) inst;
            Status status = getStatus(brInst.cond_, statusMap);
            switch (status) {
                case CONST:
                    int condValue = getConstValue(brInst.cond_, constValueMap);
                    workList.offer(new Edge(belong, condValue == 1 ? brInst.trueBlock_ : brInst.falseBlock_));
                    break;
                case NON_CONST:
                    workList.offer(new Edge(belong, brInst.trueBlock_));
                    workList.offer(new Edge(belong, brInst.falseBlock_));
                    break;
                case UNKNOWN:
                    break;
            }
            return;
        }
        Status newStatus;
        IRLocalVar def = inst.def_.iterator().next();
        Status prevStatus = statusMap.get(def);
        if (inst instanceof IRBinaryInst) {
            IRBinaryInst binaryInst = (IRBinaryInst) inst;
            Status lhsStatus = getStatus(binaryInst.lhs_, statusMap);
            Status rhsStatus = getStatus(binaryInst.rhs_, statusMap);
            newStatus = combineStatus(lhsStatus, rhsStatus);
            constValueMap.remove(def);
            if (newStatus == Status.CONST) {
                int lhsValue = getConstValue(binaryInst.lhs_, constValueMap);
                int rhsValue = getConstValue(binaryInst.rhs_, constValueMap);
                int defValue;
                // zero division error may occur in unreachable blocks
                if ((binaryInst.op_.equals("sdiv") || binaryInst.op_.equals("srem")) && rhsValue == 0) {
                    defValue = lhsValue;
                }
                else {
                    switch (binaryInst.op_) {
                        case "add":
                            defValue = lhsValue + rhsValue;
                            break;
                        case "sub":
                            defValue = lhsValue - rhsValue;
                            break;
                        case "mul":
                            defValue = lhsValue * rhsValue;
                            break;
                        case "sdiv":
                            defValue = lhsValue / rhsValue;
                            break;
                        case "srem":
                            defValue = lhsValue % rhsValue;
                            break;
                        case "and":
                            defValue = lhsValue & rhsValue;
                            break;
                        case "or":
                            defValue = lhsValue | rhsValue;
                            break;
                        case "xor":
                            defValue = lhsValue ^ rhsValue;
                            break;
                        case "shl":
                            defValue = lhsValue << rhsValue;
                            break;
                        case "ashr":
                            defValue = lhsValue >> rhsValue;
                            break;
                        default:
                            defValue = 0;
                            break;
                    }
                }
                constValueMap.put(def, defValue);
            }
        }
        else if (inst instanceof IRIcmpInst) {
            IRIcmpInst icmpInst = (IRIcmpInst) inst;
            Status lhsStatus = getStatus(icmpInst.lhs_, statusMap);
            Status rhsStatus = getStatus(icmpInst.rhs_, statusMap);
            newStatus = combineStatus(lhsStatus, rhsStatus);
            constValueMap.remove(def);
            if (newStatus == Status.CONST) {
                int lhsValue = getConstValue(icmpInst.lhs_, constValueMap);
                int rhsValue = getConstValue(icmpInst.rhs_, constValueMap);
                int defValue;
                switch (icmpInst.cond_) {
                    case "slt":
                        defValue = (lhsValue < rhsValue ? 1 : 0);
                        break;
                    case "sgt":
                        defValue = (lhsValue > rhsValue ? 1 : 0);
                        break;
                    case "sle":
                        defValue = (lhsValue <= rhsValue ? 1 : 0);
                        break;
                    case "sge":
                        defValue = (lhsValue >= rhsValue ? 1 : 0);
                        break;
                    case "eq":
                        defValue = (lhsValue == rhsValue ? 1 : 0);
                        break;
                    case "ne":
                        defValue = (lhsValue != rhsValue ? 1 : 0);
                        break;
                    default:
                        defValue = 0;
                        break;
                }
                constValueMap.put(def, defValue);
            }
        }
        else if (inst instanceof IRPhiInst) {
            IRPhiInst phiInst = (IRPhiInst) inst;
            HashSet<IRBasicBlock> executableSet = executableMap.get(belongMap.get(phiInst));
            HashSet<Status> valueStatusSet = new HashSet<>();
            for (var phiInfo : phiInst.info_.entrySet()) {
                if (executableSet.contains(phiInfo.getKey())) {
                    Status valueStatus = getStatus(phiInfo.getValue(), statusMap);
                    valueStatusSet.add(valueStatus);
                }
            }
            if (valueStatusSet.contains(Status.NON_CONST)) {
                newStatus = Status.NON_CONST;
            }
            else if (valueStatusSet.contains(Status.CONST)) {
                newStatus = Status.CONST;
            }
            else {
                newStatus = Status.UNKNOWN;
            }
            Integer defValue = null;
            if (newStatus == Status.CONST) {
                HashSet<Integer> constValueSet = new HashSet<>();
                for (var phiInfo : phiInst.info_.entrySet()) {
                    if (executableSet.contains(phiInfo.getKey()) &&
                        getStatus(phiInfo.getValue(), statusMap) == Status.CONST) {
                        constValueSet.add(getConstValue(phiInfo.getValue(), constValueMap));
                    }
                }
                if (constValueSet.size() >= 2) {
                    newStatus = Status.NON_CONST;
                }
                else {
                    defValue = constValueSet.iterator().next();
                }
            }
            constValueMap.remove(def);
            if (newStatus == Status.CONST) {
                constValueMap.put(def, defValue);
            }
        }
        else { // inst instanceof IRCallInst/IRGetElementPtrInst/IRLoadInst
            newStatus = Status.NON_CONST;
        }
        if (prevStatus != newStatus) {
            statusMap.put(def, newStatus);
            for (var useInst : useInstMap.get(def)) {
                workList.offer(useInst);
            }
        }
    }

    private Status getStatus(IRValue value, HashMap<IRLocalVar, Status> statusMap) {
        if (value instanceof IRLocalVar) {
            return statusMap.get(value);
        }
        if ((value instanceof IRGlobalVar) || (value instanceof IRNullConst)) {
            return Status.NON_CONST;
        }
        return Status.CONST;
    }

    private int getConstValue(IRValue value, HashMap<IRLocalVar, Integer> constValueMap) {
        if (value instanceof IRIntConst) {
            return ((IRIntConst) value).value_;
        }
        if (value instanceof IRBoolConst) {
            return ((IRBoolConst) value).value_ ? 1 : 0;
        }
        if (value instanceof IRLocalVar) {
            return constValueMap.get(value);
        }
        return 0;
    }

    private Status combineStatus(Status status1, Status status2) {
        if (status1 == Status.NON_CONST || status2 == Status.NON_CONST) {
            return Status.NON_CONST;
        }
        if (status1 == Status.UNKNOWN || status2 == Status.UNKNOWN) {
            return Status.UNKNOWN;
        }
        return Status.CONST;
    }

    private void substitute(IRInst inst, HashMap<IRLocalVar, Integer> constValueMap) {
        if (inst instanceof IRBinaryInst) {
            IRBinaryInst binaryInst = (IRBinaryInst) inst;
            binaryInst.lhs_ = getValueSubstitution(binaryInst.lhs_, constValueMap);
            binaryInst.rhs_ = getValueSubstitution(binaryInst.rhs_, constValueMap);
        }
        else if (inst instanceof IRBrInst) {
            IRBrInst brInst = (IRBrInst) inst;
            brInst.cond_ = getValueSubstitution(brInst.cond_, constValueMap);
        }
        else if (inst instanceof IRCallInst) {
            IRCallInst callInst = (IRCallInst) inst;
            callInst.args_.replaceAll(value -> getValueSubstitution(value, constValueMap));
        }
        else if (inst instanceof IRGetElementPtrInst) {
            IRGetElementPtrInst getElementPtrInst = (IRGetElementPtrInst) inst;
            getElementPtrInst.id1_ = getValueSubstitution(getElementPtrInst.id1_, constValueMap);
        }
        else if (inst instanceof IRIcmpInst) {
            IRIcmpInst icmpInst = (IRIcmpInst) inst;
            icmpInst.lhs_ = getValueSubstitution(icmpInst.lhs_, constValueMap);
            icmpInst.rhs_ = getValueSubstitution(icmpInst.rhs_, constValueMap);
        }
        else if (inst instanceof IRLoadInst) {
            IRLoadInst loadInst = (IRLoadInst) inst;
            loadInst.pointer_ = getValueSubstitution(loadInst.pointer_, constValueMap);
        }
        else if (inst instanceof IRPhiInst) {
            IRPhiInst phiInst = (IRPhiInst) inst;
            phiInst.info_.replaceAll((block, value) -> getValueSubstitution(value, constValueMap));
        }
        else if (inst instanceof IRRetInst) {
            IRRetInst retInst = (IRRetInst) inst;
            retInst.value_ = getValueSubstitution(retInst.value_, constValueMap);
        }
        else if (inst instanceof IRStoreInst) {
            IRStoreInst storeInst = (IRStoreInst) inst;
            storeInst.pointer_ = getValueSubstitution(storeInst.pointer_, constValueMap);
            storeInst.value_ = getValueSubstitution(storeInst.value_, constValueMap);
        }
    }

    private IRValue getValueSubstitution(IRValue value, HashMap<IRLocalVar, Integer> constValueMap) {
        if (value instanceof IRLocalVar) {
            IRLocalVar localVar = (IRLocalVar) value;
            if (constValueMap.containsKey(localVar)) {
                return localVar.type_.equals(new IRIntType(32)) ? new IRIntConst(
                    constValueMap.get(localVar)) : new IRBoolConst(constValueMap.get(localVar) == 1);
            }
        }
        return value;
    }
}
