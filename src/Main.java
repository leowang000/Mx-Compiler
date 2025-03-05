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
            FileOutputStream irNoOptimizationOutput = new FileOutputStream("test/output-00-no-optimization.ll");
            FileOutputStream irGlobalToLocalOutput = new FileOutputStream("test/output-01-global-to-local.ll");
            FileOutputStream irNoAllocaOutput = new FileOutputStream("test/output-02-no-alloca.ll");
            FileOutputStream irSCCPOutput = new FileOutputStream("test/output-03-sccp.ll");
            FileOutputStream irADCEOutput = new FileOutputStream("test/output-04-adce.ll");
            FileOutputStream irGCMOutput = new FileOutputStream("test/output-05-gcm-output.ll");
            FileOutputStream irInlineOutput = new FileOutputStream("test/output-06-inline.ll");
            FileOutputStream irSecondGlobalToLocalOutput = new FileOutputStream(
                "test/output-07-second-global-to-local.ll");
            FileOutputStream irSecondNoAllocaOutput = new FileOutputStream("test/output-08-second-no-alloca.ll");
            FileOutputStream irSecondSCCPOutput = new FileOutputStream("test/output-09-second-sccp.ll");
            FileOutputStream irSecondADCEOutput = new FileOutputStream("test/output-10-second-adce.ll");
            FileOutputStream asmOutput = new FileOutputStream("test/output.s")
        ) {
            String input_file_name = "test/test.mx";
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
            irNoOptimizationOutput.write(irProgram.toString().getBytes());
            new UnusedFunctionRemover().visit(irProgram);
            new GlobalToLocalOptimizer().visit(irProgram);
            irGlobalToLocalOutput.write(irProgram.toString().getBytes());
            new AllocaEliminator().visit(irProgram);
            irNoAllocaOutput.write(irProgram.toString().getBytes());
            new SCCPOptimizer().visit(irProgram);
            irSCCPOutput.write(irProgram.toString().getBytes());
            new ADCEOptimizer().visit(irProgram);
            irADCEOutput.write(irProgram.toString().getBytes());
            new GCMOptimizer().visit(irProgram);
            irGCMOutput.write(irProgram.toString().getBytes());
            new InlineOptimizer(35).visit(irProgram);
            new InlineOptimizer(35).visit(irProgram);
            new UnusedFunctionRemover().visit(irProgram);
            irInlineOutput.write(irProgram.toString().getBytes());
            new GlobalToLocalOptimizer().visit(irProgram);
            irSecondGlobalToLocalOutput.write(irProgram.toString().getBytes());
            new AllocaEliminator().visit(irProgram);
            irSecondNoAllocaOutput.write(irProgram.toString().getBytes());
            new SCCPOptimizer().visit(irProgram);
            irSecondSCCPOutput.write(irProgram.toString().getBytes());
            new ADCEOptimizer().visit(irProgram);
            new UnusedFunctionRemover().visit(irProgram);
            irSecondADCEOutput.write(irProgram.toString().getBytes());
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