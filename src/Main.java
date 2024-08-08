import java.io.*;
import parser.*;
import util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input_file_name = "testcases/sema/basic-package/basic-1.mx";
        CharStream input = CharStreams.fromStream(new FileInputStream(input_file_name));
        try {
            MxLexer lexer = new MxLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            MxParser parser = new MxParser(new CommonTokenStream(lexer));
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree parseTreeRoot = parser.program();
        } catch (Error er) {
            System.err.println(er.toString());
            throw new RuntimeException();
        }
    }
}