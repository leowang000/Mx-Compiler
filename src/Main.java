import java.io.*;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import asm.module.ASMProgram;
import backend.*;
import frontend.*;
import middleend.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*;
import util.*;
import util.scope.GlobalScope;

public class Main {
    public static void main(String[] args) throws Exception {
        try (
            FileOutputStream irOutput = new FileOutputStream("test/output.ll");
            FileOutputStream irGlobalToLocalOutput = new FileOutputStream("test/output-global-to-local.ll");
            FileOutputStream irNoAllocaOutput = new FileOutputStream("test/output-no-alloca.ll");
            FileOutputStream irADCEOutput = new FileOutputStream("test/output-ADCE.ll");
            FileOutputStream irOptimizedOutput = new FileOutputStream("test/output-optimized.ll");
            FileOutputStream asmOutput = new FileOutputStream("test/output.s")
        ) {
            String input_file_name = "testcases/codegen/e1.mx";
            CharStream input = CharStreams.fromStream(new FileInputStream(input_file_name));
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
            // AST -> llvm IR
            IRProgram irProgram = new IRProgram();
            new IRBuilder(globalScope, irProgram).visit(ast);
            irOutput.write(irProgram.toString().getBytes());
            new GlobalToLocalOptimizer().visit(irProgram);
            irGlobalToLocalOutput.write(irProgram.toString().getBytes());
            new AllocaEliminator().visit(irProgram);
            irNoAllocaOutput.write(irProgram.toString().getBytes());
            new ADCEOptimizer().visit(irProgram);
            irADCEOutput.write(irProgram.toString().getBytes());
            new InlineOptimizer(35).visit(irProgram);
            new InlineOptimizer(35).visit(irProgram);
            new DCEOptimizer().visit(irProgram);
            new UnusedFunctionRemover().visit(irProgram);
            irOptimizedOutput.write(irProgram.toString().getBytes());
            // llvm IR -> riscv32 asm
            ASMProgram asmProgram = new ASMProgram();
            new ASMBuilder(asmProgram).visit(irProgram);
            asmOutput.write(asmProgram.toString().getBytes());
        } catch (Error err) {
            err.printStackTrace(System.err);
            throw new RuntimeException();
        }
    }
}