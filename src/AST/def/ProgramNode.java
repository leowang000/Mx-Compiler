package AST.def;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;

public class ProgramNode extends ASTNode {
    public ArrayList<VarDefNode> varDefList_;
    public ArrayList<FuncDefNode> funcDefList_;
    public ArrayList<ClassDefNode> classDefList_;

    public ProgramNode(Position pos) {
        super(pos);
        varDefList_ = new ArrayList<>();
        funcDefList_ = new ArrayList<>();
        classDefList_ = new ArrayList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
