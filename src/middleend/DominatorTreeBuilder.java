package middleend;

import java.util.*;

import IR.module.*;

public class DominatorTreeBuilder {
    public void visit(IRProgram node, boolean antiDom) {
        for (var funcDef : node.funcDefMap_.values()) {
            for (var block : funcDef.body_) {
                block.idom_ = null;
                block.domChildren_.clear();
                block.domFrontiers_.clear();
            }
        }
        new CFGBuilder().visit(node);
        for (var funcDef : node.funcDefMap_.values()) {
            visit(funcDef, antiDom);
        }
    }

    private void visit(IRFuncDef node, boolean antiDom) {
        ArrayList<IRBasicBlock> rpo = (antiDom ? node.getAntiRPO() : node.getRPO());
        ArrayList<BitSet> doms = new ArrayList<>();
        for (int i = 0; i < rpo.size(); i++) {
            BitSet tmp = new BitSet(rpo.size());
            tmp.set(0, rpo.size());
            doms.add(tmp);
        }
        HashMap<IRBasicBlock, Integer> blockIdMap = new HashMap<>();
        for (int i = 0; i < rpo.size(); i++) {
            blockIdMap.put(rpo.get(i), i);
        }
        while (true) {
            boolean changed = false;
            for (var blockId = 0; blockId < rpo.size(); blockId++) {
                IRBasicBlock block = rpo.get(blockId);
                BitSet intersect = new BitSet(rpo.size());
                if (antiDom) {
                    if (!block.succs_.isEmpty()) {
                        intersect.set(0, rpo.size());
                        for (var succ : block.succs_) {
                            intersect.and(doms.get(blockIdMap.get(succ)));
                        }
                    }
                }
                else {
                    if (!block.preds_.isEmpty()) {
                        intersect.set(0, rpo.size());
                        for (var pred : block.preds_) {
                            intersect.and(doms.get(blockIdMap.get(pred)));
                        }
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
        for (int i = 0; i < rpo.size(); i++) {
            IRBasicBlock block = rpo.get(i);
            for (int j = doms.get(i).nextSetBit(0); j >= 0; j = doms.get(i).nextSetBit(j + 1)) {
                BitSet tmp = new BitSet(rpo.size());
                tmp.or(doms.get(i));
                tmp.xor(doms.get(j));
                if (tmp.cardinality() == 1) {
                    block.idom_ = rpo.get(j);
                    rpo.get(j).domChildren_.add(block);
                    break;
                }
            }
            BitSet union = new BitSet(rpo.size());
            for (var pred : block.preds_) {
                union.or(doms.get(blockIdMap.get(pred)));
            }
            union.set(i);
            union.xor(doms.get(i));
            for (int j = union.nextSetBit(0); j >= 0; j = union.nextSetBit(j + 1)) {
                rpo.get(j).domFrontiers_.add(block);
            }
        }
    }
}
