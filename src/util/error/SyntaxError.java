package util.error;

import util.Position;

public class SyntaxError extends Error {
    public SyntaxError(String msg, Position pos) {
        super(msg, pos);
    }

    @Override
    public String toString() {
        return "Syntax Error: " + super.toString();
    }
}
