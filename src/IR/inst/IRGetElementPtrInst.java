package IR.inst;

import IR.IRVisitor;
import IR.type.IRPtrType;
import IR.type.IRStructType;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;

public class IRGetElementPtrInst extends IRInst {
    public IRLocalVar result_;
    public IRValue ptrval_;
    public IRValue id1_;
    public int id2_;

    public IRGetElementPtrInst(String resultName, IRValue ptrval, IRValue id1) {
        result_ = new IRLocalVar(resultName, ptrval.type_);
        ptrval_ = ptrval;
        id1_ = id1;
        id2_ = -1;
    }

    public IRGetElementPtrInst(String resultName, IRValue ptrval, IRValue id1, int id2) {
        result_ = new IRLocalVar(resultName, new IRPtrType(
                ((IRStructType) ((IRPtrType) ptrval.type_).getDereferenceType()).fields_.get(id2)));
        ptrval_ = ptrval;
        id1_ = id1;
        id2_ = id2;
    }

    @Override
    public String toString() {
        if (id2_ == -1) {
            return String.format("%s = getelementptr %s, ptr %s, i32 %s", result_,
                                 ((IRPtrType) ptrval_.type_).getDereferenceType(), ptrval_, id1_);
        }
        return String.format("%s = getelementptr %s, ptr %s, i32 %s, i32 %d", result_,
                             ((IRPtrType) ptrval_.type_).getDereferenceType(), ptrval_, id1_, id2_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
