package IR.type;

import util.type.Type;

public abstract class IRType {
    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);

    public abstract int getSize();

    public static IRType toIRType(Type type) {
        if (type.name_.equals("void")) {
            return new IRVoidType();
        }
        if (type.name_.equals("null")) {
            return new IRPtrType();
        }
        IRType baseType;
        switch (type.name_) {
            case "int":
                baseType = new IRIntType(32);
                break;
            case "bool":
                baseType = new IRIntType(1);
                break;
            case "string":
                baseType = new IRPtrType(new IRIntType(8));
                break;
            default:
                baseType = new IRPtrType(new IRStructType(type.name_));
        }
        if (type.isArray_) {
            return new IRPtrType(baseType, type.dim_);
        }
        return baseType;
    }
}
