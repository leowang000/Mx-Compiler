package AST.module;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.expr.*;
import util.Pair;
import util.Position;
import util.type.Type;

public class VarDefNode extends ASTNode {
    public Type type_;
    public ArrayList<Pair<String, ExprNode>> varList_ = new ArrayList<>();

    public VarDefNode(Position pos, Type type) {
        super(pos);
        type_ = type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
