package AST;

import java.util.ArrayList;

import AST.stmt.StmtNode;
import util.Pair;
import util.Position;
import util.Type;

public class FuncDefNode extends ASTNode {
    public Type type_; // the return type
    public String name_;
    public ArrayList<Pair<Type, String>> paramList_;
    public ArrayList<StmtNode> stmtList_;

    public FuncDefNode(Position pos, Type type, String funcName) {
        super(pos);
        type_ = type;
        name_ = funcName;
        paramList_ = new ArrayList<>();
        stmtList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
