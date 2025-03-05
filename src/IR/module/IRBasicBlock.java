package IR.module;

import java.util.*;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.*;
import IR.value.IRValue;
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
    public HashSet<IRBasicBlock> domAncestors_ = new HashSet<>();
    static private int tmpCnt_ = 0;

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

    private static class Node {
        public IRValue value_;
        public Node from_ = null;
        public ArrayList<Node> to_ = new ArrayList<>();

        public Node(IRValue value) {
            value_ = value;
        }
    }

    public void insertMoveInst() {
        HashMap<IRValue, Node> nodes = new HashMap<>();
        for (var moveInst : moveList_) {
            if (!nodes.containsKey(moveInst.src_)) {
                nodes.put(moveInst.src_, new Node(moveInst.src_));
            }
            if (!nodes.containsKey(moveInst.dest_)) {
                nodes.put(moveInst.dest_, new Node(moveInst.dest_));
            }
        }
        for (var moveInst : moveList_) {
            nodes.get(moveInst.dest_).from_ = nodes.get(moveInst.src_);
            nodes.get(moveInst.src_).to_.add(nodes.get(moveInst.dest_));
        }
        ArrayList<ArrayList<Node>> cycles = new ArrayList<>();
        HashSet<Node> srcNodes = new HashSet<>();
        HashSet<Node> visited = new HashSet<>();
        for (var node : nodes.values()) {
            if (visited.contains(node)) {
                continue;
            }
            HashSet<Node> currentRoundVisited = new HashSet<>();
            Node cur = node;
            while (cur != null && !visited.contains(cur)) {
                visited.add(cur);
                currentRoundVisited.add(cur);
                if (cur.from_ == null) {
                    srcNodes.add(cur);
                }
                cur = cur.from_;
            }
            if (cur == null || !currentRoundVisited.contains(cur)) {
                continue;
            }
            ArrayList<Node> cycle = new ArrayList<>(List.of(cur));
            Node cycleEntry = cur;
            cur = cur.from_;
            while (cur != cycleEntry) {
                cycle.add(cur);
                cur = cur.from_;
            }
            cycles.add(cycle);
        }
        ArrayList<IRMoveInst> result = new ArrayList<>();
        for (var node : srcNodes) {
            for (var to : node.to_) {
                visit(to, result);
            }
        }
        for (var cycle : cycles) {
            for (int i = 0; i < cycle.size(); i++) {
                for (var to : cycle.get(i).to_) {
                    if (to == cycle.get(i == 0 ? cycle.size() - 1 : i - 1)) {
                        continue;
                    }
                    visit(to, result);
                }
            }
            if (cycle.size() == 1) {
                continue;
            }
            IRLocalVar tmpVar = new IRLocalVar(".tmp." + (tmpCnt_++), cycle.get(0).value_.type_);
            result.add(new IRMoveInst(tmpVar, cycle.get(0).value_));
            for (int i = 0; i < cycle.size() - 1; i++) {
                result.add(new IRMoveInst((IRLocalVar) cycle.get(i).value_, cycle.get(i + 1).value_));
            }
            result.add(new IRMoveInst((IRLocalVar) cycle.get(cycle.size() - 1).value_, tmpVar));
        }
        instList_.addAll(instList_.size() - 1, result);
        moveList_.clear();
    }

    private void visit(Node node, ArrayList<IRMoveInst> result) {
        for (var to : node.to_) {
            visit(to, result);
        }
        result.add(new IRMoveInst((IRLocalVar) node.value_, node.from_.value_));
    }
}
