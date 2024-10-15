package asm.util;

public class MemAddr {
    public Register base_;
    public int offset_;

    public MemAddr(Register base, int offset) {
        base_ = base;
        offset_ = offset;
    }

    public MemAddr(String base, int offset) {
        base_ = new Register(base);
        offset_ = offset;
    }

    @Override
    public String toString() {
        return String.format("%d(%s)", offset_, base_);
    }
}
