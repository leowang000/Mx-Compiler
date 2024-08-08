package AST;

import java.util.ArrayList;

import AST.expr.*;
import util.Pair;
import util.Position;
import util.Type;

public class VarDefNode extends ASTNode {
    public Type type_;
    public ArrayList<Pair<String, ExprNode>> varList;

    public VarDefNode(Position pos, Type type) {
        super(pos);
        type_ = type;
        varList = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
