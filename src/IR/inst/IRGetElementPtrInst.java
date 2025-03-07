package IR.inst;

import java.util.HashSet;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRGetElementPtrInst extends IRInst {
    public IRLocalVar result_;
    public IRValue ptr_;
    public IRValue id1_;
    public int id2_;

    public IRGetElementPtrInst(IRLocalVar result, IRValue ptr, IRValue id1) {
        result_ = result;
        ptr_ = ptr;
        id1_ = id1;
        id2_ = -1;
    }

    public IRGetElementPtrInst(IRLocalVar result, IRValue ptr, int id2) {
        result_ = result;
        ptr_ = ptr;
        id1_ = null;
        id2_ = id2;
    }

    @Override
    public String toString() {
        if (id2_ == -1) {
            return String.format("%s = getelementptr %s, ptr %s, i32 %s", result_,
                                 ((IRPtrType) ptr_.type_).getDereferenceType(), ptr_, id1_);
        }
        return String.format("%s = getelementptr %s, ptr %s, i32 0, i32 %d", result_,
                             ((IRPtrType) ptr_.type_).getDereferenceType(), ptr_, id2_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void getUseAndDef() {
        use_ = new HashSet<>();
        def_ = new HashSet<>();
        addUseVar(ptr_);
        addUseVar(id1_);
        def_.add(result_);
    }
}
