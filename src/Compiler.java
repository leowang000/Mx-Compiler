import java.io.FileOutputStream;
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
        boolean llvm = (args.length > 0 && args[0].equals("-emit-llvm"));
        boolean oj = (args.length > 0 && args[0].equals("-output-builtin"));
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
                new GlobalToLocalOptimizer().visit(irProgram);
                new AllocaEliminator().visit(irProgram);
                new SCCPOptimizer().visit(irProgram);
                new ADCEOptimizer().visit(irProgram);
                new InlineOptimizer(35).visit(irProgram);
                new InlineOptimizer(35).visit(irProgram);
                new UnusedFunctionRemover().visit(irProgram);
                new SCCPOptimizer().visit(irProgram);
                new ADCEOptimizer().visit(irProgram);
                new UnusedFunctionRemover().visit(irProgram);
                if (!oj) {
                    try (FileOutputStream log = new FileOutputStream("test/log.txt", true)) {
                        log.write(irProgram.toString().getBytes());
                        log.write(System.lineSeparator().getBytes());
                    }
                }
                if (llvm) {
                    System.out.print(irProgram);
                    return;
                }
                // llvm IR -> riscv32 asm
                ASMProgram asmProgram = new ASMProgram();
                new ASMBuilder(asmProgram).visit(irProgram);
                // output
                if (oj) {
                    System.out.println(Files.readString(Paths.get("src/builtin/builtin.s")));
                }
                System.out.print(asmProgram);
                if (!oj) {
                    try (FileOutputStream log = new FileOutputStream("test/log.txt", true)) {
                        log.write(asmProgram.toString().getBytes());
                        log.write(System.lineSeparator().getBytes());
                    }
                }
            } catch (Exception ignored) {
                // undefined behaviour exists
            }
        } catch (Error err) {
            err.printStackTrace(System.err);
            throw new RuntimeException();
        }
    }
}