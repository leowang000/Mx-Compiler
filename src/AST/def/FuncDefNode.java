package AST.def;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.stmt.StmtNode;
import util.Pair;
import util.Position;
import util.type.Type;

public class FuncDefNode extends ASTNode {
    public String name_;
    public Type returnType_;
    public ArrayList<Pair<Type, String>> paramList_;
    public ArrayList<StmtNode> stmtList_;

    public FuncDefNode(Position pos, Type returnType, String funcName) {
        super(pos);
        returnType_ = returnType;
        name_ = funcName;
        paramList_ = new ArrayList<>();
        stmtList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
