package IR.module;

import java.util.*;

import IR.IRNode;
import IR.IRVisitor;
import IR.inst.IRRetInst;
import IR.type.IRType;
import IR.value.var.IRLocalVar;
import asm.util.Register;

public class IRFuncDef extends IRNode {
    public String name_;
    public IRType returnType_;
    public ArrayList<IRLocalVar> args_ = new ArrayList<>();
    public ArrayList<IRBasicBlock> body_ = new ArrayList<>();
    public int stackSize_ = 0, maxFuncArgCnt_ = 0, callLiveOutBegin_ = 0;
    public HashMap<Register, Integer> usedSRegisterMap_ = new HashMap<>();

    public IRFuncDef(String name, IRType returnType) {
        name_ = name;
        returnType_ = returnType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(returnType_).append(" @").append(name_).append("(");
        for (int i = 0; i < args_.size(); i++) {
            sb.append(args_.get(i).type_).append(" ").append(args_.get(i));
            if (i < args_.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {\n");
        for (var block : body_) {
            sb.append(block);
        }
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    public ArrayList<IRBasicBlock> getRPO() {
        ArrayList<IRBasicBlock> res = new ArrayList<>();
        HashSet<IRBasicBlock> visited = new HashSet<>();
        getPostOrder(body_.get(0), res, visited);
        Collections.reverse(res);
        return res;
    }

    public static void getPostOrder(IRBasicBlock node, ArrayList<IRBasicBlock> po, HashSet<IRBasicBlock> visited) {
        visited.add(node);
        for (var succ : node.succs_) {
            if (!visited.contains(succ)) {
                getPostOrder(succ, po, visited);
            }
        }
        po.add(node);
    }

    public ArrayList<IRBasicBlock> getAntiRPO() {
        ArrayList<IRBasicBlock> res = new ArrayList<>();
        HashSet<IRBasicBlock> visited = new HashSet<>();
        for (var block : body_) {
            if (block.instList_.get(block.instList_.size() - 1) instanceof IRRetInst) {
                getAntiPostOrder(block, res, visited);
            }
        }
        Collections.reverse(res);
        return res;
    }

    public static void getAntiPostOrder(IRBasicBlock node, ArrayList<IRBasicBlock> po, HashSet<IRBasicBlock> visited) {
        visited.add(node);
        for (var pred : node.preds_) {
            if (!visited.contains(pred)) {
                getAntiPostOrder(pred, po, visited);
            }
        }
        po.add(node);
    }
}
