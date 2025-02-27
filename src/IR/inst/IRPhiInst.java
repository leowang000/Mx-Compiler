package IR.inst;

import java.util.*;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.type.IRIntType;
import IR.type.IRPtrType;
import IR.value.IRValue;
import IR.value.constant.*;
import IR.value.var.IRLocalVar;

public class IRPhiInst extends IRInst {
    public IRLocalVar result_;
    public HashMap<IRBasicBlock, IRValue> info_ = new HashMap<>();

    public IRPhiInst(IRLocalVar result) {
        result_ = result;
    }

    public IRPhiInst(IRLocalVar result, IRBasicBlock belong) {
        result_ = result;
        for (var block : belong.preds_) {
            IRValue defaultValue;
            if (result_.type_ instanceof IRPtrType) {
                defaultValue = new IRNullConst();
            }
            else if (result_.type_.equals(new IRIntType(32))) {
                defaultValue = new IRIntConst(0);
            }
            else {
                defaultValue = new IRBoolConst(false);
            }
            info_.put(block, defaultValue);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(result_).append(" = phi ").append(result_.type_).append(" ");
        int i = 0;
        for (var entry : info_.entrySet()) {
            sb.append("[").append(entry.getValue()).append(", ").append(entry.getKey().getLabel()).append("]");
            if (i < info_.size() - 1) {
                sb.append(", ");
            }
            i++;
        }
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void getUseAndDef() {
        use_ = new HashSet<>();
        def_ = new HashSet<>();
        for (var value : info_.values()) {
            addUseVar(value);
        }
        def_.add(result_);
    }
}
