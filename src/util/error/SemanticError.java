package util.error;

import util.Position;

public class SemanticError extends Error {
    public SemanticError(String msg, Position pos) {
        super(msg, pos);
    }

    @Override
    public String toString() {
        return "Semantic Error: " + super.toString();
    }
}
