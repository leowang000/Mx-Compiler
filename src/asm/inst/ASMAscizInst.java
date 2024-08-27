package asm.inst;

public class ASMAscizInst extends ASMInst {
    public String value_;
    private final String printValue_;

    public ASMAscizInst(String value) {
        value_ = value;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                default:
                    sb.append(ch);
            }
        }
        printValue_ = sb.toString();
    }

    @Override
    public String toString() {
        return String.format(".asciz\t\"%s\"", printValue_);
    }
}
