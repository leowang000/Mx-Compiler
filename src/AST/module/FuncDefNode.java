package AST.module;

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
    public ArrayList<Pair<Type, String>> paramList_ = new ArrayList<>();
    public ArrayList<StmtNode> stmtList_ = new ArrayList<>();

    public FuncDefNode(Position pos, Type returnType, String funcName) {
        super(pos);
        returnType_ = returnType;
        name_ = funcName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
