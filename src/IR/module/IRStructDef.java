package IR.module;

import java.util.HashSet;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRStructType;

public class IRStructDef extends IRNode {
    public IRStructType struct_;
    public HashSet<String> memberFuncSet_ = new HashSet<>();
    public boolean hasConstructor_;

    public IRStructDef(String name, boolean hasConstructor) {
        struct_ = new IRStructType(name);
        hasConstructor_ = hasConstructor;
    }

    @Override
    public String toString() {
        return String.format("%s = type %s\n", struct_, struct_.getStructInfo());
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
