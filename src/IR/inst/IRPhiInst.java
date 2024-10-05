package IR.inst;

import java.util.ArrayList;

import IR.IRVisitor;
import IR.module.IRBasicBlock;
import IR.value.IRValue;
import IR.value.var.IRLocalVar;
import util.Pair;

public class IRPhiInst extends IRInst {
    public IRLocalVar result_;
    public ArrayList<Pair<IRValue, IRBasicBlock>> infoList_ = new ArrayList<>();

    public IRPhiInst(IRLocalVar result) {
        result_ = result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(result_).append(" = phi ").append(result_.type_).append(" ");
        for (int i = 0; i < infoList_.size(); i++) {
            sb.append("[").append(infoList_.get(i).first_).append(", ").append(infoList_.get(i).second_.getLabel())
                    .append("]");
            if (i < infoList_.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
