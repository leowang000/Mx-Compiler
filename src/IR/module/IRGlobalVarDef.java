package IR.module;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRIntType;
import IR.type.IRPtrType;
import IR.type.IRType;
import IR.value.var.IRGlobalVar;

public class IRGlobalVarDef extends IRNode {
    public IRGlobalVar var_;

    public IRGlobalVarDef(String varName, IRType type) {
        var_ = new IRGlobalVar(varName, type);
    }

    @Override
    public String toString() {
        IRType baseType = ((IRPtrType) var_.type_).base_;
        if (baseType instanceof IRIntType) {
            return String.format("%s = global %s 0\n", var_, baseType);
        }
        return String.format("%s = global ptr null\n", var_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
