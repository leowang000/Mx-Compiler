package IR.module;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRType;
import IR.value.var.IRGlobalVar;

public class IRGlobalVarDef extends IRNode {
    public IRGlobalVar var_;

    public IRGlobalVarDef(String varName, IRType type) {
        var_ = new IRGlobalVar(varName, type);
    }

    @Override
    public String toString() {
        return String.format("%s = global %s %s\n", var_, var_.type_, var_.type_.getDefaultValue());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
