package IR.module;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRIntType;
import IR.type.IRPtrType;
import IR.value.var.IRGlobalVar;

public class IRStringLiteralDef extends IRNode {
    public IRGlobalVar result_;
    public String value_;
    private String printValue_;

    public IRStringLiteralDef(IRGlobalVar result, String value) {
        result_ = result;
        value_ = value;
        printValue_ = value.replace("\n", "\\0A").replace("\\", "\\\\").replace("\"", "\\22");
    }

    @Override
    public String toString() {
        return String.format("%s = private unnamed_addr constant [%d x i8] c\"%s\\00\"\n", result_, value_.length() + 1,
                             printValue_);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
