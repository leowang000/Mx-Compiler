package util;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Position {
    private final int row_, col_;

    public Position(int row, int col) {
        row_ = row;
        col_ = col;
    }

    public Position(Token token) {
        row_ = token.getLine();
        col_ = token.getCharPositionInLine();
    }

    public Position(ParserRuleContext ctx) {
        row_ = ctx.getStart().getLine();
        col_ = ctx.getStart().getCharPositionInLine();
    }

    public Position(TerminalNode node) {
        row_ = node.getSymbol().getLine();
        col_ = node.getSymbol().getCharPositionInLine();
    }

    public int getRow() {
        return row_;
    }

    public int getCol() {
        return col_;
    }

    public String toString() {
        return String.format("(%d:%d)", row_, col_);
    }
}
