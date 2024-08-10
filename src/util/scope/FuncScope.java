package util.scope;

import util.Position;
import util.type.Type;
import util.error.SemanticError;

public class FuncScope extends Scope {
    public Type returnType_;

    public FuncScope(Scope parent, Type returnType) {
        super(parent);
        returnType_ = returnType;
    }

    @Override
    public void checkReturnType(Type type, Position pos) {
        if (!returnType_.equals(type)) {
            throw new SemanticError(
                    String.format("Return Type Mismatch Error: should return %s, but return %s", returnType_, type),
                    pos);
        }
    }
}
