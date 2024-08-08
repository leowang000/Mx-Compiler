package AST;

import java.util.ArrayList;
import java.util.HashMap;

import util.Position;

public class ClassDefNode extends ASTNode {
    public String name_;
    public ConstructorDefNode constructor_ = null;
    public ArrayList<FuncDefNode> funcDefList_;
    public ArrayList<VarDefNode> varDefList_;
    public HashMap<String, FuncDefNode> funcDefMap_;
    public HashMap<String, VarDefNode> varDefMap_;

    public ClassDefNode(Position pos, String className) {
        super(pos);
        name_ = className;
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
