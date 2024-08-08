package util;

public class Type {
    public final String name_;
    public final int dim_;
    public final boolean isClass_;
    public final boolean isArray_;

    public Type(String name) {
        name_ = name;
        dim_ = 0;
        isClass_ = isIdentifier(name);
        isArray_ = false;
    }

    public Type(String name, int dim) {
        name_ = name;
        dim_ = dim;
        isClass_ = isIdentifier(name);
        isArray_ = (dim > 0);
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
            (name_.equals("null") && !isIdentifier(otherType.name_)) ||
            (otherType.name_.equals("null") && !isIdentifier(name_))) {
            return true;
        }
        return name_.equals(((Type) other).name_) && dim_ == ((Type) other).dim_;
    }

    private static boolean isIdentifier(String name) {
        return !name.equals("null") && !name.equals("void") && !name.equals("int") && !name.equals("bool") &&
               !name.equals("string");
    }
}
