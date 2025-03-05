package middleend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.value.var.IRLocalVar;

// Before this pass is used, ADCE/DCE must be run.
public class GCMOptimizer {
    public void visit(IRProgram node) {
        node.reset();
        new DominatorTreeBuilder().visit(node, false, true, true);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        HashMap<IRBasicBlock, Integer> loopDepthMap = getLoopDepthMap(node);
        HashMap<IRBasicBlock, Integer> blockDomDepthMap = getBlockDomDepthMap(node);
        HashSet<IRInst> pinnedInstSet = getPinnedInstSet(node);
        HashMap<IRLocalVar, IRInst> defInstMap = new HashMap<>();
        HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap = new HashMap<>();
        getDefInstMapAndUseInstListMap(node, defInstMap, useInstListMap);
        HashMap<IRInst, IRBasicBlock> targetMap = new HashMap<>();
        scheduleEarly(node, blockDomDepthMap, pinnedInstSet, defInstMap, targetMap);
        scheduleLate(node, blockDomDepthMap, pinnedInstSet, useInstListMap, loopDepthMap, targetMap);
        scheduleInst(node, useInstListMap, targetMap);
    }

    private HashMap<IRBasicBlock, Integer> getLoopDepthMap(IRFuncDef node) {
        HashMap<IRBasicBlock, HashSet<IRBasicBlock>> headBodyMap = new HashMap<>();
        for (var block : node.body_) {
            for (var succ : block.succs_) {
                if (block.domAncestors_.contains(succ)) {
                    if (headBodyMap.containsKey(succ)) {
                        headBodyMap.get(succ).add(block);
                    }
                    else {
                        headBodyMap.put(succ, new HashSet<>(List.of(block)));
                    }
                }
            }
        }
        for (var entry : headBodyMap.entrySet()) {
            IRBasicBlock head = entry.getKey();
            HashSet<IRBasicBlock> body = entry.getValue();
            Stack<IRBasicBlock> stack = new Stack<>();
            for (var block : body) {
                stack.push(block);
            }
            while (!stack.isEmpty()) {
                IRBasicBlock block = stack.pop();
                body.add(block);
                if (block == head) {
                    continue;
                }
                for (var pred : block.preds_) {
                    if (!body.contains(pred)) {
                        stack.push(pred);
                    }
                }
            }
        }
        HashMap<IRBasicBlock, Integer> headDepthMap = new HashMap<>();
        for (var head : headBodyMap.entrySet()) {
            headDepthMap.put(head.getKey(), 0);
        }
        for (var head1 : headBodyMap.keySet()) {
            for (var body2 : headBodyMap.values()) {
                if (body2.contains(head1)) {
                    headDepthMap.put(head1, headDepthMap.get(head1) + 1);
                }
            }
        }
        HashMap<IRBasicBlock, Integer> loopDepthMap = new HashMap<>();
        for (var block : node.body_) {
            int depth = 0;
            for (var entry : headDepthMap.entrySet()) {
                if (headBodyMap.get(entry.getKey()).contains(block)) {
                    depth = Math.max(depth, entry.getValue());
                }
            }
            loopDepthMap.put(block, depth);
        }
        return loopDepthMap;
    }

    private HashMap<IRBasicBlock, Integer> getBlockDomDepthMap(IRFuncDef node) {
        HashMap<IRBasicBlock, Integer> blockDomDepthMap = new HashMap<>();
        markDomDepth(node.body_.get(0), 0, blockDomDepthMap);
        return blockDomDepthMap;
    }

    private void markDomDepth(IRBasicBlock block, int depth, HashMap<IRBasicBlock, Integer> blockDomDepthMap) {
        blockDomDepthMap.put(block, depth);
        for (var child : block.domChildren_) {
            markDomDepth(child, depth + 1, blockDomDepthMap);
        }
    }

    private HashSet<IRInst> getPinnedInstSet(IRFuncDef node) {
        HashSet<IRInst> pinnedInstSet = new HashSet<>();
        for (var block : node.body_) {
            pinnedInstSet.addAll(block.phiMap_.values());
            for (var inst : block.instList_) {
                if ((inst instanceof IRLoadInst) || (inst instanceof IRStoreInst) || (inst instanceof IRCallInst) ||
                    (inst instanceof IRBrInst) || (inst instanceof IRJumpInst) || (inst instanceof IRRetInst)) {
                    pinnedInstSet.add(inst);
                }
            }
        }
        return pinnedInstSet;
    }

    private void getDefInstMapAndUseInstListMap(IRFuncDef node, HashMap<IRLocalVar, IRInst> defInstMap,
                                                HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap) {
        for (var block : node.body_) {
            for (var phiInst : block.phiMap_.values()) {
                phiInst.getUseAndDef();
                for (var def : phiInst.def_) {
                    defInstMap.put(def, phiInst);
                }
            }
            for (var inst : block.instList_) {
                inst.getUseAndDef();
                for (var def : inst.def_) {
                    defInstMap.put(def, inst);
                }
            }
        }
        for (var localVar : defInstMap.keySet()) {
            useInstListMap.put(localVar, new ArrayList<>());
        }
        for (var block : node.body_) {
            for (var phiInst : block.phiMap_.values()) {
                for (var use : phiInst.use_) {
                    if (useInstListMap.containsKey(use)) {
                        useInstListMap.get(use).add(phiInst);
                    }
                }
            }
            for (var inst : block.instList_) {
                for (var use : inst.use_) {
                    if (useInstListMap.containsKey(use)) {
                        useInstListMap.get(use).add(inst);
                    }
                }
            }
        }
    }

    private void scheduleEarly(IRFuncDef node, HashMap<IRBasicBlock, Integer> blockDomDepthMap,
                               HashSet<IRInst> pinnedInstSet, HashMap<IRLocalVar, IRInst> defInstMap,
                               HashMap<IRInst, IRBasicBlock> targetMap) {
        IRBasicBlock entry = node.body_.get(0);
        for (var block : node.body_) {
            for (var phiInst : block.phiMap_.values()) {
                targetMap.put(phiInst, block);
            }
            for (var inst : block.instList_) {
                targetMap.put(inst, pinnedInstSet.contains(inst) ? block : entry);
            }
        }
        HashSet<IRInst> visited = new HashSet<>();
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                scheduleEarly(inst, entry, visited, blockDomDepthMap, pinnedInstSet, defInstMap, targetMap);
            }
        }
    }

    private void scheduleEarly(IRInst inst, IRBasicBlock entry, HashSet<IRInst> visited,
                               HashMap<IRBasicBlock, Integer> blockDomDepthMap, HashSet<IRInst> pinnedInstSet,
                               HashMap<IRLocalVar, IRInst> defInstMap, HashMap<IRInst, IRBasicBlock> targetMap) {
        if (pinnedInstSet.contains(inst) || visited.contains(inst)) {
            return;
        }
        visited.add(inst);
        IRBasicBlock block = entry;
        for (var use : inst.use_) {
            if (defInstMap.containsKey(use)) {
                IRInst defInst = defInstMap.get(use);
                scheduleEarly(defInst, entry, visited, blockDomDepthMap, pinnedInstSet, defInstMap, targetMap);
                if (blockDomDepthMap.get(targetMap.get(defInst)) > blockDomDepthMap.get(block)) {
                    block = targetMap.get(defInst);
                }
            }
        }
        targetMap.put(inst, block);
    }

    private void scheduleLate(IRFuncDef node, HashMap<IRBasicBlock, Integer> blockDomDepthMap,
                              HashSet<IRInst> pinnedInstSet, HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap,
                              HashMap<IRBasicBlock, Integer> loopDepthMap, HashMap<IRInst, IRBasicBlock> targetMap) {
        HashSet<IRInst> visited = new HashSet<>();
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                scheduleLate(inst, visited, blockDomDepthMap, pinnedInstSet, useInstListMap, loopDepthMap, targetMap);
            }
        }
    }

    private void scheduleLate(IRInst inst, HashSet<IRInst> visited, HashMap<IRBasicBlock, Integer> blockDomDepthMap,
                              HashSet<IRInst> pinnedInstSet, HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap,
                              HashMap<IRBasicBlock, Integer> loopDepthMap, HashMap<IRInst, IRBasicBlock> targetMap) {
        if (pinnedInstSet.contains(inst) || visited.contains(inst)) {
            return;
        }
        visited.add(inst);
        IRBasicBlock block = null;
        for (var def : inst.def_) {
            for (var useInst : useInstListMap.get(def)) {
                scheduleLate(useInst, visited, blockDomDepthMap, pinnedInstSet, useInstListMap, loopDepthMap,
                             targetMap);
                IRBasicBlock useBlock = targetMap.get(useInst);
                if (useInst instanceof IRPhiInst) {
                    for (var info : ((IRPhiInst) useInst).info_.entrySet()) {
                        if (def.equals(info.getValue())) {
                            useBlock = info.getKey();
                            break;
                        }
                    }
                }
                block = (block == null ? useBlock : getLCA(block, useBlock, blockDomDepthMap));
            }
        }
        IRBasicBlock best = block;
        while (true) {
            if (loopDepthMap.get(block) < loopDepthMap.get(best)) {
                best = block;
            }
            if (block == targetMap.get(inst)) {
                break;
            }
            block = block.idom_;
        }
        targetMap.put(inst, best);
    }

    private IRBasicBlock getLCA(IRBasicBlock block1, IRBasicBlock block2,
                                HashMap<IRBasicBlock, Integer> blockDomDepthMap) {
        while (blockDomDepthMap.get(block1) < blockDomDepthMap.get(block2)) {
            block2 = block2.idom_;
        }
        while (blockDomDepthMap.get(block2) < blockDomDepthMap.get(block1)) {
            block1 = block1.idom_;
        }
        while (block1 != block2) {
            block1 = block1.idom_;
            block2 = block2.idom_;
        }
        return block1;
    }

    private void scheduleInst(IRFuncDef node, HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap,
                              HashMap<IRInst, IRBasicBlock> targetMap) {
        HashSet<IRInst> visited = new HashSet<>();
        ArrayList<IRInst> allInstSet = new ArrayList<>();
        HashMap<IRInst, IRBasicBlock> instBlockMap = new HashMap<>();
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                instBlockMap.put(inst, block);
            }
            allInstSet.addAll(block.instList_);
            block.instList_.removeIf(inst -> targetMap.get(inst) != block);
        }
        for (var inst : allInstSet) {
            scheduleInst(inst, visited, instBlockMap, useInstListMap, targetMap);
        }
    }

    private void scheduleInst(IRInst inst, HashSet<IRInst> visited, HashMap<IRInst, IRBasicBlock> instBlockMap,
                              HashMap<IRLocalVar, ArrayList<IRInst>> useInstListMap,
                              HashMap<IRInst, IRBasicBlock> targetMap) {
        if (visited.contains(inst) || targetMap.get(inst) == instBlockMap.get(inst)) {
            return;
        }
        visited.add(inst);
        HashSet<IRBasicBlock> useInstTargetSet = new HashSet<>();
        for (var def : inst.def_) {
            for (var useInst : useInstListMap.get(def)) {
                if (!(useInst instanceof IRPhiInst)) {
                    useInstTargetSet.add(targetMap.get(useInst));
                    scheduleInst(useInst, visited, instBlockMap, useInstListMap, targetMap);
                }
            }
        }
        IRBasicBlock targetBlock = targetMap.get(inst);
        if (useInstTargetSet.contains(targetBlock)) {
            targetBlock.instList_.add(0, inst);
        }
        else {
            targetBlock.instList_.add(targetBlock.instList_.size() - 1, inst);
        }
    }
}
