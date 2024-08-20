package util.scope;

import util.Position;
import util.type.Type;
import util.error.SemanticError;

public class FuncScope extends Scope {
    public Type returnType_;
    public boolean isReturned_ = false;

    public FuncScope(Scope parent, Type returnType) {
        super(parent);
        returnType_ = returnType;
    }

    @Override
    public Type getReturnType() {
        isReturned_ = true;
        return returnType_;
    }
}
