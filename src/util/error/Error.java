package util.error;

import util.Position;

abstract public class Error extends RuntimeException {
    private final String msg_;
    private final Position pos_;

    Error(String msg, Position pos) {
        msg_ = msg;
        pos_ = pos;
    }

    @Override
    public String toString() {
        return String.format("%s at %s", msg_, pos_.toString());
    }

}
