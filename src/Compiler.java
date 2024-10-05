import java.nio.file.Files;
import java.nio.file.Paths;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import asm.module.ASMProgram;
import backend.*;
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
            // program -> AST
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
            // AST -> llvm IR
            IRProgram irProgram = new IRProgram();
            new IRBuilder(globalScope, irProgram).visit(ast);
            new UnusedFunctionRemover().visit(irProgram);
            new CFGBuilder().visit(irProgram);
            // llvm IR -> asm
            new StackManager().visit(irProgram);
            ASMProgram asmProgram = new ASMProgram();
            new ASMBuilder(asmProgram).visit(irProgram);
            // output
            System.out.println(Files.readString(Paths.get("src/builtin/builtin.s")));
            System.out.print(asmProgram);
        } catch (Error err) {
            System.err.println(err);
            throw new RuntimeException();
        }
    }
}