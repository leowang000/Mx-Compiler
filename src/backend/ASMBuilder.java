package backend;

import java.util.*;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import IR.value.IRValue;
import IR.value.constant.*;
import IR.value.var.IRGlobalVar;
import IR.value.var.IRLocalVar;
import asm.inst.*;
import asm.module.ASMBlock;
import asm.module.ASMProgram;
import asm.util.Register;
import middleend.CFGBuilder;
import util.Pair;

public class ASMBuilder implements IRVisitor {
    private final ASMProgram asmProgram_;
    private ASMBlock currentBlock_ = null;
    private IRFuncDef belong_ = null;
    private final Register t0_ = new Register("t0"), t1_ = new Register("t1");
    private final Register ra_ = new Register("ra"), sp_ = new Register("sp");
    private static final ArrayList<Register> aRegisterList_ = new ArrayList<>();

    static {
        for (int i = 0; i <= 7; i++) {
            aRegisterList_.add(LinearScanRegAllocator.freeRegisters_.get(24 - i));
        }
    }

    public ASMBuilder(ASMProgram asmProgram) {
        asmProgram_ = asmProgram;
    }

    @Override
    public void visit(IRProgram node) {
        node.reset();
        new CFGBuilder().visit(node);
        new PhiResolver().visit(node);
        new LinearScanRegAllocator().visit(node);
        for (var globalVarDef : node.globalVarList_) {
            globalVarDef.accept(this);
        }
        for (var arrayLiteral : node.arrayLiteralList_) {
            arrayLiteral.accept(this);
        }
        for (var stringLiteral : node.stringLiteralList_) {
            stringLiteral.accept(this);
        }
        for (var fStringFragment : node.fStringList_) {
            fStringFragment.accept(this);
        }
        for (var funcDef : node.funcDefMap_.values()) {
            funcDef.accept(this);
        }
    }

    @Override
    public void visit(IRFuncDecl node) {}

    @Override
    public void visit(IRFuncDef node) {
        belong_ = node;
        for (var block : node.body_) {
            block.accept(this);
        }
    }

    @Override
    public void visit(IRGlobalVarDef node) {
        ASMBlock newBlock = new ASMBlock(node.var_.name_);
        newBlock.instList_.add(new ASMWordInst(0));
        asmProgram_.data_.blockList_.add(newBlock);
    }

    @Override
    public void visit(IRStringLiteralDef node) {
        ASMBlock newBlock = new ASMBlock(node.result_.name_);
        newBlock.instList_.add(new ASMAscizInst(node.value_));
        asmProgram_.rodata_.blockList_.add(newBlock);
    }

    @Override
    public void visit(IRStructDef node) {}

    @Override
    public void visit(IRBasicBlock node) {
        if (node == belong_.body_.get(0)) {
            currentBlock_ = new ASMBlock(belong_.name_);
            currentBlock_.info_ = "\n\t.globl " + belong_.name_;
            addAddiInst(sp_, sp_, -belong_.stackSize_);
            addSwInst(ra_, sp_, belong_.stackSize_ - 4);
            for (var sReg : belong_.usedSRegisterMap_.keySet()) {
                storeSRegister(sReg);
            }
        }
        else {
            currentBlock_ = new ASMBlock(".L." + node.label_);
        }
        for (var inst : node.instList_) {
            inst.accept(this);
        }
    }

    @Override
    public void visit(IRAllocaInst node) {}

    @Override
    public void visit(IRBinaryInst node) {
        if (node.result_.isUnused()) {
            return;
        }
        Register tmp1 = loadVarHint(t0_, node.lhs_);
        Register tmp2 = loadVarHint(t1_, node.rhs_);
        String op;
        switch (node.op_) {
            case "sdiv":
                op = "div";
                break;
            case "srem":
                op = "rem";
                break;
            case "shl":
                op = "sll";
                break;
            case "ashr":
                op = "sra";
                break;
            default:
                op = node.op_;
        }
        if (node.result_.register_ != null) {
            currentBlock_.instList_.add(new ASMBinaryInst(op, node.result_.register_, tmp1, tmp2));
        }
        else {
            currentBlock_.instList_.add(new ASMBinaryInst(op, t1_, tmp1, tmp2));
            addSwInst(t1_, sp_, node.result_.stackOffset_);
        }
    }

    @Override
    public void visit(IRBrInst node) {
        Register tmp = loadVarHint(t0_, node.cond_);
        currentBlock_.instList_.add(new ASMUnaryBranchInst("bnez", tmp, ".L." + node.trueBlock_.label_));
        currentBlock_.instList_.add(new ASMJInst(".L." + node.falseBlock_.label_));
        asmProgram_.text_.blockList_.add(currentBlock_);
    }

    @Override
    public void visit(IRCallInst node) {
        for (int i = 0; i < Math.min(belong_.args_.size(), 8); i++) {
            storeARegister(aRegisterList_.get(i));
        }
        HashMap<Register, Integer> storedCallLiveOutIdMap = new HashMap<>();
        int cnt = 0;
        for (var variable : node.out_) {
            if (variable != node.result_ && variable.register_ != null && variable.register_.name_.charAt(0) != 's') {
                int id = cnt++;
                storedCallLiveOutIdMap.put(variable.register_, id);
                storeCallLiveOut(variable.register_, id);
            }
        }
        for (int i = 8; i < node.args_.size(); i++) {
            Register tmp = loadVarHint(t1_, node.args_.get(i));
            addSwInst(tmp, sp_, 4 * (i - 8));
        }
        ArrayList<Pair<Register, Register>> mvInstList = new ArrayList<>();
        for (int i = 0; i < Math.min(node.args_.size(), 8); i++) {
            if ((node.args_.get(i) instanceof IRLocalVar) && ((IRLocalVar) node.args_.get(i)).register_ != null) {
                mvInstList.add(new Pair<>(aRegisterList_.get(i), ((IRLocalVar) node.args_.get(i)).register_));
            }
        }
        addCallMvInst(mvInstList);
        for (int i = 0; i < Math.min(node.args_.size(), 8); i++) {
            if (!(node.args_.get(i) instanceof IRLocalVar) || ((IRLocalVar) node.args_.get(i)).register_ == null) {
                loadVar(aRegisterList_.get(i), node.args_.get(i));
            }
        }
        currentBlock_.instList_.add(new ASMCallInst(node.funcName_));
        if (node.result_ != null && !node.result_.isUnused()) {
            if (node.result_.register_ != null) {
                currentBlock_.instList_.add(new ASMUnaryInst("mv", node.result_.register_, aRegisterList_.get(0)));
            }
            else {
                addSwInst(aRegisterList_.get(0), sp_, node.result_.stackOffset_);
            }
        }
        for (var entry : storedCallLiveOutIdMap.entrySet()) {
            loadCallLiveOut(entry.getKey(), entry.getValue());
        }
        for (int i = 0; i < Math.min(belong_.args_.size(), 8); i++) {
            loadARegister(aRegisterList_.get(i));
        }
    }

    @Override
    public void visit(IRGetElementPtrInst node) {
        if (node.result_.isUnused()) {
            return;
        }
        Register rd = (node.result_.register_ != null ? node.result_.register_ : t1_);
        if (node.id2_ == -1) {
            Register ptr = loadVarHint(t0_, node.ptr_);
            loadVar(t1_, node.id1_);
            currentBlock_.instList_.add(new ASMBinaryImmInst("slli", t1_, t1_, 2));
            currentBlock_.instList_.add(new ASMBinaryInst("add", rd, ptr, t1_));
        }
        else {
            Register ptr = loadVarHint(t1_, node.ptr_);
            addAddiInst(rd, ptr, 4 * node.id2_);
        }
        if (node.result_.register_ == null) {
            addSwInst(t1_, sp_, node.result_.stackOffset_);
        }
    }

    @Override
    public void visit(IRIcmpInst node) {
        if (node.result_.isUnused()) {
            return;
        }
        Register tmp1 = loadVarHint(t0_, node.lhs_);
        Register tmp2 = loadVarHint(t1_, node.rhs_);
        Register rd = (node.result_.register_ != null ? node.result_.register_ : t1_);
        switch (node.cond_) {
            case "eq":
                currentBlock_.instList_.add(new ASMBinaryInst("xor", rd, tmp1, tmp2));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", rd, rd));
                break;
            case "ne":
                currentBlock_.instList_.add(new ASMBinaryInst("xor", rd, tmp1, tmp2));
                currentBlock_.instList_.add(new ASMUnaryInst("snez", rd, rd));
                break;
            case "slt":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", rd, tmp1, tmp2));
                break;
            case "sgt":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", rd, tmp2, tmp1));
                break;
            case "sle":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", rd, tmp2, tmp1));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", rd, rd));
                break;
            case "sge":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", rd, tmp1, tmp2));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", rd, rd));
                break;
        }
        if (node.result_.register_ == null) {
            addSwInst(t1_, sp_, node.result_.stackOffset_);
        }
    }

    @Override
    public void visit(IRJumpInst node) {
        currentBlock_.instList_.add(new ASMJInst(".L." + node.destBlock_.label_));
        asmProgram_.text_.blockList_.add(currentBlock_);
    }

    @Override
    public void visit(IRLoadInst node) {
        if (node.result_.isUnused()) {
            return;
        }
        Register ptr = loadVarHint(t0_, node.pointer_);
        if (node.result_.register_ != null) {
            addLwInst(node.result_.register_, ptr, 0);
        }
        else {
            addLwInst(t1_, ptr, 0);
            addSwInst(t1_, sp_, node.result_.stackOffset_);
        }
    }

    @Override
    public void visit(IRMoveInst node) {
        if (node.dest_.isUnused()) {
            return;
        }
        if (node.dest_.register_ != null) {
            loadVar(node.dest_.register_, node.src_);
        }
        else {
            loadVar(t1_, node.src_);
            addSwInst(t1_, sp_, node.dest_.stackOffset_);
        }
    }

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {
        if (node.value_ != null) {
            loadVar(aRegisterList_.get(0), node.value_);
        }
        for (var sReg : belong_.usedSRegisterMap_.keySet()) {
            loadSRegister(sReg);
        }
        addLwInst(ra_, sp_, belong_.stackSize_ - 4);
        addAddiInst(sp_, sp_, belong_.stackSize_);
        currentBlock_.instList_.add(new ASMRetInst());
        asmProgram_.text_.blockList_.add(currentBlock_);
    }

    @Override
    public void visit(IRStoreInst node) {
        Register val = loadVarHint(t0_, node.value_);
        Register ptr = loadVarHint(t1_, node.pointer_);
        addSwInst(val, ptr, 0);
    }

    private void loadVar(Register rd, IRValue value) {
        if (value instanceof IRIntConst) {
            currentBlock_.instList_.add(new ASMLiInst(rd, ((IRIntConst) value).value_));
            return;
        }
        if (value instanceof IRBoolConst) {
            currentBlock_.instList_.add(new ASMLiInst(rd, ((IRBoolConst) value).value_ ? 1 : 0));
            return;
        }
        if (value instanceof IRNullConst) {
            currentBlock_.instList_.add(new ASMLiInst(rd, 0));
            return;
        }
        if (value instanceof IRGlobalVar) {
            currentBlock_.instList_.add(new ASMLaInst(rd, ((IRGlobalVar) value).name_));
            return;
        }
        IRLocalVar localVar = (IRLocalVar) value;
        if (localVar.register_ != null) {
            currentBlock_.instList_.add(new ASMUnaryInst("mv", rd, localVar.register_));
            return;
        }
        if (localVar.isAllocaResult_) {
            addAddiInst(rd, sp_, localVar.stackOffset_);
            return;
        }
        addLwInst(rd, sp_, localVar.stackOffset_);
    }

    private Register loadVarHint(Register hint, IRValue value) {
        if (value instanceof IRIntConst) {
            currentBlock_.instList_.add(new ASMLiInst(hint, ((IRIntConst) value).value_));
            return hint;
        }
        if (value instanceof IRBoolConst) {
            currentBlock_.instList_.add(new ASMLiInst(hint, ((IRBoolConst) value).value_ ? 1 : 0));
            return hint;
        }
        if (value instanceof IRNullConst) {
            currentBlock_.instList_.add(new ASMLiInst(hint, 0));
            return hint;
        }
        if (value instanceof IRGlobalVar) {
            currentBlock_.instList_.add(new ASMLaInst(hint, ((IRGlobalVar) value).name_));
            return hint;
        }
        IRLocalVar localVar = (IRLocalVar) value;
        if (localVar.register_ != null) {
            return localVar.register_;
        }
        if (localVar.isAllocaResult_) {
            addAddiInst(hint, sp_, localVar.stackOffset_);
            return hint;
        }
        addLwInst(hint, sp_, localVar.stackOffset_);
        return hint;
    }

    private void loadCallLiveOut(Register reg, int id) {
        addLwInst(reg, sp_, belong_.callLiveOutBegin_ + 4 * id);
    }

    private void storeCallLiveOut(Register reg, int id) {
        addSwInst(reg, sp_, belong_.callLiveOutBegin_ + 4 * id);
    }

    private void loadSRegister(Register reg) {
        addLwInst(reg, sp_, belong_.usedSRegisterMap_.get(reg));
    }

    private void storeSRegister(Register reg) {
        addSwInst(reg, sp_, belong_.usedSRegisterMap_.get(reg));
    }

    private void loadARegister(Register reg) {
        addLwInst(reg, sp_, 4 * (Math.max(belong_.maxFuncArgCnt_ - 8, 0) + (reg.name_.charAt(1) - '0')));
    }

    private void storeARegister(Register reg) {
        addSwInst(reg, sp_, 4 * (Math.max(belong_.maxFuncArgCnt_ - 8, 0) + (reg.name_.charAt(1) - '0')));
    }

    private void addAddiInst(Register rd, Register rs, int imm) { // may use t0
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMBinaryImmInst("addi", rd, rs, imm));
        }
        else {
            if (rd != rs) {
                currentBlock_.instList_.add(new ASMLiInst(rd, imm));
                currentBlock_.instList_.add(new ASMBinaryInst("add", rd, rs, rd));
            }
            else {
                currentBlock_.instList_.add(new ASMLiInst(t0_, imm));
                currentBlock_.instList_.add(new ASMBinaryInst("add", rd, rs, t0_));
            }
        }
    }

    private void addSwInst(Register rs, Register base, int imm) { // may use t0
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMSwInst(rs, base, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst(t0_, imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", t0_, base, t0_));
            currentBlock_.instList_.add(new ASMSwInst(rs, t0_, 0));
        }
    }

    private void addLwInst(Register rd, Register base, int imm) { // may use t0
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMLwInst(rd, base, imm));
        }
        else {
            if (rd != base) {
                currentBlock_.instList_.add(new ASMLiInst(rd, imm));
                currentBlock_.instList_.add(new ASMBinaryInst("add", rd, base, rd));
                currentBlock_.instList_.add(new ASMLwInst(rd, rd, 0));
            }
            else {
                currentBlock_.instList_.add(new ASMLiInst(t0_, imm));
                currentBlock_.instList_.add(new ASMBinaryInst("add", rd, base, t0_));
                currentBlock_.instList_.add(new ASMLwInst(rd, rd, 0));
            }
        }
    }

    private static class Node {
        public Register reg_;
        public Node from_ = null;
        public ArrayList<Node> to_ = new ArrayList<>();

        public Node(Register reg) {
            reg_ = reg;
        }
    }

    private void addCallMvInst(ArrayList<Pair<Register, Register>> mvInstList) {
        HashMap<Register, Node> nodes = new HashMap<>();
        for (var moveInst : mvInstList) {
            if (!nodes.containsKey(moveInst.first_)) {
                nodes.put(moveInst.first_, new Node(moveInst.first_));
            }
            if (!nodes.containsKey(moveInst.second_)) {
                nodes.put(moveInst.second_, new Node(moveInst.second_));
            }
        }
        for (var moveInst : mvInstList) {
            nodes.get(moveInst.first_).from_ = nodes.get(moveInst.second_);
            nodes.get(moveInst.second_).to_.add(nodes.get(moveInst.first_));
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
        for (var node : srcNodes) {
            for (var to : node.to_) {
                addCallMvInst(to);
            }
        }
        for (var cycle : cycles) {
            for (int i = 0; i < cycle.size(); i++) {
                for (var to : cycle.get(i).to_) {
                    if (to == cycle.get(i == 0 ? cycle.size() - 1 : i - 1)) {
                        continue;
                    }
                    addCallMvInst(to);
                }
            }
            if (cycle.size() == 1) {
                continue;
            }
            currentBlock_.instList_.add(new ASMUnaryInst("mv", t0_, cycle.get(0).reg_));
            for (int i = 0; i < cycle.size() - 1; i++) {
                currentBlock_.instList_.add(new ASMUnaryInst("mv", cycle.get(i).reg_, cycle.get(i + 1).reg_));
            }
            currentBlock_.instList_.add(new ASMUnaryInst("mv", cycle.get(cycle.size() - 1).reg_, t0_));
        }
    }

    private void addCallMvInst(Node node) {
        for (var to : node.to_) {
            addCallMvInst(to);
        }
        currentBlock_.instList_.add(new ASMUnaryInst("mv", node.reg_, node.from_.reg_));
    }
}
