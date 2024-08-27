package backend;

import IR.IRVisitor;
import IR.inst.*;
import IR.module.*;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.constant.*;
import IR.value.var.IRGlobalVar;
import IR.value.var.IRLocalVar;
import asm.inst.*;
import asm.module.ASMBlock;
import asm.module.ASMProgram;
import asm.util.MemAddr;

public class ASMBuilder implements IRVisitor {
    private final boolean needLongBranch = true;
    private final ASMProgram asmProgram_;
    private ASMBlock currentBlock_ = null;
    private IRFuncDef belong_ = null;
    private boolean isFirstBlock_ = false;

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
        currentBlock_ = new ASMBlock(node.name_);
        currentBlock_.info_ = "\n\t.globl " + node.name_;
        addAddi("sp", "sp", -node.stackSize_, "t6");
        addSw("ra", "sp", node.stackSize_ - 4, "t6");
        isFirstBlock_ = true;
        for (var block : node.body_) {
            block.accept(this);
            isFirstBlock_ = false;
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
        if (!isFirstBlock_) {
            currentBlock_ = new ASMBlock(node.label_);
        }
        for (var inst : node.instList_) {
            inst.accept(this);
        }
    }

    @Override
    public void visit(IRAllocaInst node) {}

    @Override
    public void visit(IRBinaryInst node) {
        loadVar("t0", node.lhs_);
        loadVar("t1", node.rhs_);
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
        currentBlock_.instList_.add(new ASMBinaryInst(op, "t0", "t0", "t1"));
        storeRegisterIntoLocalVar("t0", node.result_);
    }

    @Override
    public void visit(IRBrInst node) {
        if (needLongBranch) {
            int id = ASMBranchInst.cnt_++;
            String label = ".tmp_" + id;
            loadVar("t0", node.cond_);
            currentBlock_.instList_.add(new ASMBranchInst("bne", "t0", "x0", label));
            currentBlock_.instList_.add(new ASMJInst(node.falseBlock_.label_));
            asmProgram_.text_.blockList_.add(currentBlock_);
            currentBlock_ = new ASMBlock(label);
            currentBlock_.instList_.add(new ASMJInst(node.trueBlock_.label_));
            asmProgram_.text_.blockList_.add(currentBlock_);
        }
        else {
            loadVar("t0", node.cond_);
            currentBlock_.instList_.add(new ASMBranchInst("bne", "t0", "x0", node.trueBlock_.label_));
            currentBlock_.instList_.add(new ASMJInst(node.falseBlock_.label_));
            asmProgram_.text_.blockList_.add(currentBlock_);
        }
    }

    @Override
    public void visit(IRCallInst node) {
        for (int i = 0; i < Math.min(node.args_.size(), 8); i++) {
            storeRegisterA(i);
        }
        for (int i = 0; i < node.args_.size(); i++) {
            if (i < 8) {
                loadVar("s" + (i + 1), node.args_.get(i));
            }
            else {
                loadVar("t0", node.args_.get(i));
                addSw("t0", "sp", 4 * (i - 8), "t6");
            }
        }
        for (int i = 0; i < node.args_.size() && i < 8; i++) {
            currentBlock_.instList_.add(new ASMUnaryInst("mv", "a" + i, "s" + (i + 1)));
        }
        currentBlock_.instList_.add(new ASMCallInst(node.funcName_));
        if (node.result_ != null) {
            storeRegisterIntoLocalVar("a0", node.result_);
        }
        for (int i = 0; i < Math.min(node.args_.size(), 8); i++) {
            loadRegisterA(i);
        }
    }

    @Override
    public void visit(IRGetElementPtrInst node) {
        MemAddr addr = loadPtrAddr("t0", node.ptr_);
        if (node.id2_ == -1) {
            loadVar("t1", node.id1_);
            currentBlock_.instList_.add(
                    new ASMLiInst("t2", ((IRPtrType) node.result_.type_).getDereferenceType().getSize()));
            currentBlock_.instList_.add(new ASMBinaryInst("mul", "t1", "t1", "t2"));
            currentBlock_.instList_.add(new ASMBinaryInst("add", "t0", "t0", "t1"));
        }
        else {
            addr.offset_ += 4 * node.id2_;
            addAddi("t0", addr.base_.name_, addr.offset_, "t6");
        }
        storeRegisterIntoLocalVar("t0", node.result_);
    }

    @Override
    public void visit(IRIcmpInst node) {
        loadVar("t0", node.lhs_);
        loadVar("t1", node.rhs_);
        switch (node.cond_) {
            case "eq":
                currentBlock_.instList_.add(new ASMBinaryInst("xor", "t0", "t0", "t1"));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", "t0", "t0"));
                break;
            case "ne":
                currentBlock_.instList_.add(new ASMBinaryInst("xor", "t0", "t0", "t1"));
                currentBlock_.instList_.add(new ASMUnaryInst("snez", "t0", "t0"));
                break;
            case "slt":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", "t0", "t0", "t1"));
                break;
            case "sgt":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", "t0", "t1", "t0"));
                break;
            case "sle":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", "t0", "t1", "t0"));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", "t0", "t0"));
                break;
            case "sge":
                currentBlock_.instList_.add(new ASMBinaryInst("slt", "t0", "t0", "t1"));
                currentBlock_.instList_.add(new ASMUnaryInst("seqz", "t0", "t0"));
                break;
        }
        storeRegisterIntoLocalVar("t0", node.result_);
    }

    @Override
    public void visit(IRJumpInst node) {
        currentBlock_.instList_.add(new ASMJInst(node.destBlock_.label_));
        asmProgram_.text_.blockList_.add(currentBlock_);
    }

    @Override
    public void visit(IRLoadInst node) {
        MemAddr addr = loadPtrAddr("t0", node.pointer_);
        addLw("t1", addr, "t6");
        storeRegisterIntoLocalVar("t1", node.result_);
    }

    @Override
    public void visit(IRPhiInst node) {}

    @Override
    public void visit(IRRetInst node) {
        if (node.value_ != null) {
            loadVar("a0", node.value_);
        }
        addLw("ra", "sp", belong_.stackSize_ - 4, "t6");
        addAddi("sp", "sp", belong_.stackSize_, "t6");
        currentBlock_.instList_.add(new ASMRetInst());
        asmProgram_.text_.blockList_.add(currentBlock_);
    }

    @Override
    public void visit(IRSelectInst node) {}

    @Override
    public void visit(IRStoreInst node) {
        loadVar("t0", node.value_);
        MemAddr addr = loadPtrAddr("t1", node.pointer_);
        addSw("t0", addr, "t6");
    }

    private void loadRegisterA(int i) {
        addLw("a" + i, "sp", belong_.stackSize_ - 8 - 4 * i, "t6");
    }

    private void storeRegisterA(int i) {
        addSw("a" + i, "sp", belong_.stackSize_ - 8 - 4 * i, "t6");
    }

    private MemAddr loadPtrAddr(String rd, IRValue value) {
        if (value instanceof IRGlobalVar) {
            currentBlock_.instList_.add(new ASMLaInst(rd, ((IRGlobalVar) value).name_));
            return new MemAddr(rd, 0);
        }
        IRLocalVar localVar = (IRLocalVar) value;
        if (localVar.register_ != null) {
            return new MemAddr(localVar.register_, 0);
        }
        if (localVar.isAllocaResult_) {
            return new MemAddr("sp", localVar.stackOffset_);
        }
        addLw(rd, "sp", localVar.stackOffset_, "t6");
        return new MemAddr(rd, 0);
    }

    private void loadVar(String rd, IRValue value) {
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
            addAddi("rd", "sp", localVar.stackOffset_, "t6");
            return;
        }
        addLw(rd, "sp", localVar.stackOffset_, "t6");
    }

    private void storeRegisterIntoLocalVar(String rs, IRLocalVar localVar) {
        addSw(rs, "sp", localVar.stackOffset_, "t6");
    }

    private void addAddi(String rd, String rs, int imm, String tmp) {
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMBinaryImmInst("addi", rd, rs, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst(tmp, imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", rd, rs, tmp));
        }
    }

    private void addSw(String rs, String base, int imm, String tmp) {
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMSwInst(rs, base, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst(tmp, imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", tmp, base, tmp));
            currentBlock_.instList_.add(new ASMSwInst(rs, tmp, 0));
        }
    }

    private void addSw(String rs, MemAddr addr, String tmp) {
        addSw(rs, addr.base_.name_, addr.offset_, tmp);
    }

    private void addLw(String rd, String base, int imm, String tmp) {
        if (imm >= -2048 && imm <= 2047) {
            currentBlock_.instList_.add(new ASMLwInst(rd, base, imm));
        }
        else {
            currentBlock_.instList_.add(new ASMLiInst(tmp, imm));
            currentBlock_.instList_.add(new ASMBinaryInst("add", tmp, base, tmp));
            currentBlock_.instList_.add(new ASMLwInst(rd, tmp, 0));
        }
    }

    private void addLw(String rd, MemAddr addr, String tmp) {
        addLw(rd, addr.base_.name_, addr.offset_, tmp);
    }
}
