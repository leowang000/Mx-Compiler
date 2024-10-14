package backend;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import asm.inst.*;
import asm.module.ASMBlock;
import asm.module.ASMProgram;
import asm.util.Register;

public class ASMBuilder implements IRVisitor {
    private final ASMProgram asmProgram_;
    private ASMBlock currentBlock_ = null;
    private IRFuncDef belong_ = null;

    public ASMBuilder(ASMProgram asmProgram) {
        asmProgram_ = asmProgram;
    }

    @Override
    public void visit(IRProgram node) {
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
            block.insertMoveInst();
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
            currentBlock_.info_ = "\n\t.globl" + belong_.name_;
            addAddiInst("sp", "sp", -belong_.stackSize_);
            addSwInst("ra", "sp", belong_.stackSize_ - 4);
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
    public void visit(IRAllocaInst node) {

    }

    @Override
    public void visit(IRBinaryInst node) {

    }

    @Override
    public void visit(IRBrInst node) {

    }

    @Override
    public void visit(IRCallInst node) {

    }

    @Override
    public void visit(IRGetElementPtrInst node) {

    }

    @Override
    public void visit(IRIcmpInst node) {

    }

    @Override
    public void visit(IRJumpInst node) {

    }

    @Override
    public void visit(IRLoadInst node) {

    }

    @Override
    public void visit(IRMoveInst node) {

    }

    @Override
    public void visit(IRPhiInst node) {

    }

    @Override
    public void visit(IRRetInst node) {

    }

    @Override
    public void visit(IRStoreInst node) {

    }

    private void loadSRegister(Register reg) {
        addLwInst(reg.name_, "sp", belong_.usedSRegisterMap_.get(reg));
    }

    private void storeSRegister(Register reg) {
        addSwInst(reg.name_, "sp", belong_.usedSRegisterMap_.get(reg));
    }

    private void loadRegisterA(Register reg) {
        addLwInst(reg.name_, "sp", 4 * (reg.name_.charAt(1) - '0'));
    }

    private void storeRegisterA(Register reg) {
        addSwInst(reg.name_, "sp", 4 * (reg.name_.charAt(1) - '0'));
    }

    private void addAddiInst(String rd, String rs, int imm) { // may use t1
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMBinaryImmInst("addi", rd, rs, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst("t1", imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", rd, rs, "t1"));
        }
    }

    private void addSwInst(String rs, String base, int imm) { // may use t1
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMSwInst(rs, base, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst("t1", imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", "t1", base, "t1"));
            currentBlock_.instList_.add(new ASMSwInst(rs, "t1", 0));
        }
    }

    private void addLwInst(String rd, String base, int imm) { // may use t1
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMLwInst(rd, base, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst("t1", imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", "t1", base, "t1"));
            currentBlock_.instList_.add(new ASMLwInst(rd, "t1", 0));
        }
    }
}
