package util.type;

import parser.MxParser;

public class Type {
    public final String name_;
    public final int dim_;
    public final boolean isClass_;
    public final boolean isArray_;

    public Type(String name) {
        name_ = name;
        dim_ = 0;
        isClass_ = isBuiltInType(name);
        isArray_ = false;
    }

    public Type(String name, int dim) {
        name_ = name;
        dim_ = dim;
        isClass_ = isBuiltInType(name);
        isArray_ = (dim > 0);
    }

    public Type(MxParser.ReturntypeContext ctx) {
        if (ctx.Void() != null) {
            name_ = "void";
            dim_ = 0;
            isClass_ = false;
            isArray_ = false;
        }
        else {
            name_ = ctx.type().typeName().getText();
            dim_ = ctx.type().LeftBracket().size();
            isClass_ = isBuiltInType(name_);
            isArray_ = (dim_ > 0);
        }
    }

    public Type(MxParser.TypeContext ctx) {
        name_ = ctx.typeName().getText();
        dim_ = ctx.LeftBracket().size();
        isClass_ = isBuiltInType(name_);
        isArray_ = (dim_ > 0);
    }

    public Type(MxParser.TypeNameContext ctx) {
        name_ = ctx.getText();
        dim_ = 0;
        isClass_ = isBuiltInType(name_);
        isArray_ = false;
    }

    public Type(Type other) {
        name_ = other.name_;
        dim_ = other.dim_;
        isClass_ = other.isClass_;
        isArray_ = other.isArray_;
    }

    public boolean Equals(Object other) {
        if (!(other instanceof Type)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        Type otherType = (Type) other;
        if ((name_.equals("null") && otherType.name_.equals("null")) ||
            (name_.equals("null") && !isBuiltInType(otherType.name_)) ||
            (otherType.name_.equals("null") && !isBuiltInType(name_))) {
            return true;
        }
        return name_.equals(((Type) other).name_) && dim_ == ((Type) other).dim_;
    }

    @Override
    public String toString() {
        return name_ + "[]".repeat(dim_);
    }

    private static boolean isBuiltInType(String name) {
        return !name.equals("null") && !name.equals("void") && !name.equals("int") && !name.equals("bool") &&
               !name.equals("string");
    }
}
