package AST.def;

import java.util.ArrayList;
import java.util.HashMap;

import AST.ASTNode;
import AST.ASTVisitor;
import AST.expr.ExprNode;
import util.Pair;
import util.Position;
import util.type.Type;

public class ClassDefNode extends ASTNode {
    public String name_;
    public ConstructorDefNode constructor_;
    public ArrayList<FuncDefNode> funcDefList_;
    public ArrayList<VarDefNode> varDefList_;
    public HashMap<String, FuncDefNode> funcDefMap_;
    public HashMap<String, Pair<Type, ExprNode>> varDefMap_;

    public ClassDefNode(Position pos, String className, ConstructorDefNode constructor) {
        super(pos);
        name_ = className;
        constructor_ = constructor;
        funcDefList_ = new ArrayList<>();
        varDefList_ = new ArrayList<>();
        funcDefMap_ = new HashMap<>();
        varDefMap_ = new HashMap<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
