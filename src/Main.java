import java.io.*;

import AST.module.ProgramNode;
import IR.module.IRProgram;
import frontend.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import parser.*;
import util.*;
import util.scope.GlobalScope;

public class Main {
    public static void main(String[] args) throws Exception {
        String input_file_name = "testcases/sema/const-array-package/const-array6.mx";
        FileOutputStream output = new FileOutputStream("my-output/output.txt");
        CharStream input = CharStreams.fromStream(new FileInputStream(input_file_name));
        try {
            MxLexer lexer = new MxLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree parseTreeRoot = parser.program();
            ASTBuilder astBuilder = new ASTBuilder();
            ProgramNode ast = (ProgramNode) astBuilder.visit(parseTreeRoot);
            GlobalScope globalScope = new GlobalScope();
            new SymbolCollector(globalScope).visit(ast);
            new SemanticChecker(globalScope).visit(ast);
            IRProgram irProgram = new IRProgram();
            IRBuilder irBuilder = new IRBuilder(globalScope, irProgram);
            irBuilder.visit(ast);
            output.write(irProgram.toString().getBytes());
        } catch (Error err) {
            System.err.println(err);
            throw new RuntimeException();
        }
    }
}