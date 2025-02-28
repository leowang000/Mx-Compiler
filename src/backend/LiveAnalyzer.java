package backend;

import java.util.*;

import IR.inst.*;
import IR.module.*;
import IR.value.var.IRLocalVar;
import middleend.CFGBuilder;

// When LiveAnalyzer is used, phi instructions must have been resolved.
public class LiveAnalyzer {
    private HashMap<IRInst, ArrayList<IRInst>> succMap_ = null, predMap_ = null;

    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    private void visit(IRFuncDef node) {
        buildInstGraph(node);
        executeLiveAnalysis(node);
    }

    private void buildInstGraph(IRFuncDef node) {
        succMap_ = new HashMap<>();
        predMap_ = new HashMap<>();
        for (int i = 0; i < node.body_.size(); i++) {
            IRBasicBlock block = node.body_.get(i);
            for (int j = 0; j < block.instList_.size(); j++) {
                IRInst inst = block.instList_.get(j);
                predMap_.put(inst, new ArrayList<>());
                if (j < block.instList_.size() - 1) {
                    succMap_.put(inst, new ArrayList<>(List.of(block.instList_.get(j + 1))));
                    continue;
                }
                ArrayList<IRInst> succs = new ArrayList<>();
                if (inst instanceof IRRetInst && i < node.body_.size() - 1) {
                    succs.add(node.body_.get(i + 1).instList_.get(0));
                }
                else if (inst instanceof IRJumpInst) {
                    succs.add(((IRJumpInst) inst).destBlock_.instList_.get(0));
                }
                else if (inst instanceof IRBrInst) {
                    succs.add(((IRBrInst) inst).trueBlock_.instList_.get(0));
                    succs.add(((IRBrInst) inst).falseBlock_.instList_.get(0));
                }
                succMap_.put(inst, succs);
            }
        }
        for (var entry : succMap_.entrySet()) {
            for (var succ : entry.getValue()) {
                predMap_.get(succ).add(entry.getKey());
            }
        }
    }

    private void executeLiveAnalysis(IRFuncDef node) {
        ArrayList<IRRetInst> exitList = new ArrayList<>();
        executeLiveAnalysisInitialization(node, exitList);
        HashSet<IRLocalVar> args = new HashSet<>(node.args_);
        while (true) {
            boolean changed = false;
            ArrayDeque<IRInst> queue = new ArrayDeque<>(exitList);
            HashSet<IRInst> visited = new HashSet<>(exitList);
            while (!queue.isEmpty()) {
                IRInst inst = queue.poll();
                for (var pred : predMap_.get(inst)) {
                    if (!visited.contains(pred)) {
                        visited.add(pred);
                        queue.offer(pred);
                    }
                }
                HashSet<IRLocalVar> tmpOut = new HashSet<>();
                for (var succ : succMap_.get(inst)) {
                    tmpOut.addAll(succ.in_);
                }
                HashSet<IRLocalVar> tmpIn = new HashSet<>(tmpOut);
                tmpIn.removeAll(inst.def_);
                tmpIn.addAll(inst.use_);
                tmpIn.removeAll(args); // live in and live out do not contain function arguments
                if (tmpIn.size() != inst.in_.size() || tmpOut.size() != inst.out_.size()) {
                    changed = true;
                }
                inst.in_ = tmpIn;
                inst.out_ = tmpOut;
            }
            if (!changed) {
                break;
            }
        }
    }

    private void executeLiveAnalysisInitialization(IRFuncDef node, ArrayList<IRRetInst> exitList) {
        for (var block : node.body_) {
            for (var inst : block.instList_) {
                inst.getUseAndDef();
                if (inst instanceof IRRetInst) {
                    exitList.add((IRRetInst) inst);
                }
            }
        }
    }
}
