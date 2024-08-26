import java.nio.file.Files;
import java.nio.file.Paths;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import asm.module.ASMProgram;
import backend.ASMBuilder;
import backend.StackManager;
import frontend.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.MxLexer;
import parser.MxParser;
import util.MxErrorListener;
import util.scope.GlobalScope;

public class Compiler {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromStream(System.in);
        try {
            MxLexer lexer = new MxLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree parseTreeRoot = parser.program();
            ProgramNode ast = (ProgramNode) new ASTBuilder().visit(parseTreeRoot);
            GlobalScope globalScope = new GlobalScope();
            new SymbolCollector(globalScope).visit(ast);
            new SemanticChecker(globalScope).visit(ast);
            IRProgram irProgram = new IRProgram();
            IRBuilder irBuilder = new IRBuilder(globalScope, irProgram);
            irBuilder.visit(ast);
            StackManager stackManager = new StackManager();
            stackManager.visit(irProgram);
            ASMProgram asmProgram = new ASMProgram();
            new ASMBuilder(asmProgram).visit(irProgram);
            String builtin = Files.readString(Paths.get("src/builtin/builtin.s"));
//            System.out.println(builtin);
            System.out.println(asmProgram);
        } catch (Error err) {
            System.err.println(err);
            throw new RuntimeException();
        }
    }
}