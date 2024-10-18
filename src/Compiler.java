import java.nio.file.Files;
import java.nio.file.Paths;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import asm.module.ASMProgram;
import backend.*;
import frontend.*;
import middleend.*;
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
            // Mx* -> AST
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
            try {
                // AST -> llvm IR
                IRProgram irProgram = new IRProgram();
                new IRBuilder(globalScope, irProgram).visit(ast);
                new UnusedFunctionRemover().visit(irProgram);
                new CFGBuilder().visit(irProgram);
                new DominatorTreeBuilder().visit(irProgram);
                new AllocaEliminator().visit(irProgram);
                if (args.length > 0 && args[0].equals("-emit-llvm")) {
                    System.out.print(irProgram);
                    return;
                }
                // llvm IR -> riscv32 asm
                new PhiResolver().visit(irProgram);
                new LinearScanRegAllocator().visit(irProgram);
                ASMProgram asmProgram = new ASMProgram();
                new ASMBuilder(asmProgram).visit(irProgram);
                // output
                if (args.length > 0 && args[0].equals("-output-builtin")) {
                    System.out.println(Files.readString(Paths.get("src/builtin/builtin.s")));
                }
                System.out.print(asmProgram);
            } catch (Exception ignored) {
                // undefined behaviour exists
            }
        } catch (Error err) {
            err.printStackTrace(System.err);
            throw new RuntimeException();
        }
    }
}