package AST.module;

import java.util.ArrayList;

import AST.ASTNode;
import AST.ASTVisitor;
import util.Position;
import util.scope.ClassScope;

public class ClassDefNode extends ASTNode {
    public String name_;
    public ConstructorDefNode constructor_;
    public ArrayList<FuncDefNode> funcDefList_ = new ArrayList<>();
    public ArrayList<VarDefNode> varDefList_ = new ArrayList<>();
    public ClassScope scope_ = null;

    public ClassDefNode(Position pos, String className, ConstructorDefNode constructor) {
        super(pos);
        name_ = className;
        constructor_ = constructor;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
