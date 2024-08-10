package AST.def;

import java.util.ArrayList;
import java.util.HashMap;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;

public class ProgramNode extends ASTNode {
    public ArrayList<VarDefNode> varDefList_;
    public ArrayList<FuncDefNode> funcDefList_;
    public ArrayList<ClassDefNode> classDefList_;
    public HashMap<String, VarDefNode> varDefMap_;
    public HashMap<String, FuncDefNode> funcDefMap_;
    public HashMap<String, ClassDefNode> classDefMap_;

    public ProgramNode(Position pos) {
        super(pos);
        varDefList_ = new ArrayList<>();
        funcDefList_ = new ArrayList<>();
        classDefList_ = new ArrayList<>();
        varDefMap_ = new HashMap<>();
        funcDefMap_ = new HashMap<>();
        classDefMap_ = new HashMap<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
