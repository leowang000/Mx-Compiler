package middleend;

import java.util.*;

import IR.inst.IRRetInst;
import IR.module.*;

public class DominatorTreeBuilder {
    public void visit(IRProgram node, boolean antiDom) {
        node.reset();
        new CFGBuilder().visit(node);
        if (antiDom) {
            for (var funcDef : node.funcDefMap_.values()) {
                buildAntiDom(funcDef);
            }
        }
        else {
            for (var funcDef : node.funcDefMap_.values()) {
                buildDom(funcDef);
            }
        }
    }

    private void buildDom(IRFuncDef node) {
        ArrayList<IRBasicBlock> rpo = node.getRPO();
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
                if (!block.preds_.isEmpty()) {
                    intersect.set(0, rpo.size());
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

    private void buildAntiDom(IRFuncDef node) {
        IRBasicBlock sink = new IRBasicBlock("", null);
        for (var block : node.body_) {
            if (block.instList_.get(block.instList_.size() - 1) instanceof IRRetInst) {
                block.succs_.add(sink);
                sink.preds_.add(block);
            }
        }
        ArrayList<IRBasicBlock> rpo = node.getAntiRPO(sink);
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
                if (!block.succs_.isEmpty()) {
                    intersect.set(0, rpo.size());
                    for (var succ : block.succs_) {
                        intersect.and(doms.get(blockIdMap.get(succ)));
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
            for (var succ : block.succs_) {
                union.or(doms.get(blockIdMap.get(succ)));
            }
            union.set(i);
            union.xor(doms.get(i));
            for (int j = union.nextSetBit(0); j >= 0; j = union.nextSetBit(j + 1)) {
                rpo.get(j).domFrontiers_.add(block);
            }
        }
        for (var block : node.body_) {
            block.succs_.remove(sink);
        }
    }
}
