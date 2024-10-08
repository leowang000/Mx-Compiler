package middleend;

import java.util.*;

import IR.module.*;

public class DominatorTreeBuilder {
    public void visit(IRProgram node) {
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef);
        }
    }

    public void visit(IRFuncDef node) {
        ArrayList<IRBasicBlock> postOrder = new ArrayList<>();
        HashSet<IRBasicBlock> visited = new HashSet<>();
        getPostOrder(node.body_.get(0), postOrder, visited);
        ArrayList<BitSet> doms = new ArrayList<>();
        for (int i = 0; i < node.body_.size(); i++) {
            BitSet tmp = new BitSet(node.body_.size());
            tmp.set(0, node.body_.size());
            doms.add(tmp);
        }
        HashMap<IRBasicBlock, Integer> blockIdMap = new HashMap<>();
        for (int i = 0; i < node.body_.size(); i++) {
            blockIdMap.put(node.body_.get(i), i);
        }
        while (true) {
            boolean changed = false;
            for (int i = postOrder.size() - 1; i >= 0; i--) {
                IRBasicBlock block = postOrder.get(i);
                int blockId = blockIdMap.get(block);
                BitSet intersect = new BitSet(node.body_.size());
                if (!block.preds_.isEmpty()) {
                    intersect.set(0, node.body_.size());
                    for (var pred : block.preds_) {
                        intersect.and(doms.get(blockIdMap.get(pred)));
                    }
                }
                intersect.set(blockId);
                if (!doms.get(blockId).equals(intersect)) {
                    doms.set(blockId, intersect);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        for (int i = 0; i < node.body_.size(); i++) {
            IRBasicBlock block = node.body_.get(i);
            for (int j = doms.get(i).nextSetBit(0); j >= 0; j = doms.get(i).nextSetBit(j + 1)) {
                BitSet tmp = new BitSet(node.body_.size());
                tmp.or(doms.get(i));
                tmp.xor(doms.get(j));
                if (tmp.cardinality() == 1) {
                    block.idom_ = node.body_.get(j);
                    node.body_.get(j).domChildren_.add(block);
                    break;
                }
            }
            BitSet union = new BitSet(node.body_.size());
            for (var pred : block.preds_) {
                union.or(doms.get(blockIdMap.get(pred)));
            }
            union.set(i);
            union.xor(doms.get(i));
            for (int j = union.nextSetBit(0); j >= 0; j = union.nextSetBit(j + 1)) {
                node.body_.get(j).domFrontiers_.add(block);
            }
        }
    }

    public void getPostOrder(IRBasicBlock node, ArrayList<IRBasicBlock> postOrder, HashSet<IRBasicBlock> visited) {
        visited.add(node);
        for (var succ : node.succs_) {
            if (!visited.contains(succ)) {
                getPostOrder(succ, postOrder, visited);
            }
        }
        postOrder.add(node);
    }
}
