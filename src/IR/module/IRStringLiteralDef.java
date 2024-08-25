package IR.module;

import IR.IRNode;
import IR.IRVisitor;
import IR.value.var.IRGlobalVar;

public class IRStringLiteralDef extends IRNode {
    public IRGlobalVar result_;
    public String value_;
    private String printValue_;

    public IRStringLiteralDef(IRGlobalVar result, String value) {
        result_ = result;
        value_ = value;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < value.length()) {
            char ch = value.charAt(i++);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\0A");
                    break;
                case '\"':
                    sb.append("\\22");
                    break;
                default:
                    sb.append(ch);
            }
        }
        printValue_ = sb.toString();
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
