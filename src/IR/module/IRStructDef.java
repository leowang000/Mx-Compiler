package IR.module;

import java.util.ArrayList;

import IR.IRNode;
import IR.IRVisitor;
import IR.type.IRStructType;
import IR.type.IRType;

public class IRStructDef extends IRNode {
    public IRStructType struct_;

    public IRStructDef(String name, ArrayList<IRType> fields) {
        struct_ = new IRStructType(name, fields);
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
