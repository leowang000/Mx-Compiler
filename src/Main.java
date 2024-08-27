import java.io.*;
import java.nio.charset.StandardCharsets;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import asm.module.ASMProgram;
import backend.ASMBuilder;
import backend.StackManager;
import frontend.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*;
import util.*;
import util.scope.GlobalScope;

public class Main {
    public static void main(String[] args) throws Exception {
        String input_file_name = "testcases/sema/ternary-package/ternary-expression-5.mx";
        FileOutputStream irOutput = new FileOutputStream("test/output.ll");
        FileOutputStream asmOutput = new FileOutputStream("test/output.s");
        CharStream input = CharStreams.fromStream(new FileInputStream(input_file_name));
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
            irOutput.write(irProgram.toString().getBytes());
            StackManager stackManager = new StackManager();
            stackManager.visit(irProgram);
            ASMProgram asmProgram = new ASMProgram();
            new ASMBuilder(asmProgram).visit(irProgram);
            asmOutput.write(asmProgram.toString().getBytes());
        } catch (Error err) {
            System.err.println(err);
            throw new RuntimeException();
        }
    }
}