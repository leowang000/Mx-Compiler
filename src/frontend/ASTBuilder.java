package frontend;

import AST.*;
import AST.def.*;
import AST.expr.*;
import AST.expr.atom.*;
import AST.stmt.*;
import parser.MxBaseVisitor;
import parser.MxParser;
import util.Pair;
import util.Position;
import util.error.SemanticError;
import util.error.SyntaxError;
import util.type.Type;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(MxParser.ProgramContext ctx) {
        ProgramNode program = new ProgramNode(new Position(ctx));
        for (var funcDef : ctx.funcDef()) {
            FuncDefNode funcDefNode = (FuncDefNode) visit(funcDef);
            program.funcDefList_.add(funcDefNode);
        }
        for (var varDef : ctx.varDef()) {
            VarDefNode varDefNode = (VarDefNode) visit(varDef);
            program.varDefList_.add(varDefNode);
        }
        for (var classDef : ctx.classDef()) {
            ClassDefNode classDefNode = (ClassDefNode) visit(classDef);
            program.classDefList_.add(classDefNode);
        }
        return program;
    }

    @Override
    public ASTNode visitClassDef(MxParser.ClassDefContext ctx) {
        if (ctx.constructorDef().size() >= 2) {
            throw new SemanticError("Constructor Redefinition Error: " + ctx.Identifier().getText(),
                                    new Position(ctx));
        }
        ConstructorDefNode constructor =
                (ctx.constructorDef().isEmpty() ? null : (ConstructorDefNode) visit(ctx.constructorDef(0)));
        ClassDefNode classDef = new ClassDefNode(new Position(ctx), ctx.Identifier().getText(), constructor);
        for (var varDef : ctx.varDef()) {
            classDef.varDefList_.add((VarDefNode) visit(varDef));
        }
        for (var funcDef : ctx.funcDef()) {
            classDef.funcDefList_.add((FuncDefNode) visit(funcDef));
        }
        return classDef;
    }

    @Override
    public ASTNode visitConstructorDef(MxParser.ConstructorDefContext ctx) {
        ConstructorDefNode constructor = new ConstructorDefNode(new Position(ctx), ctx.Identifier().getText());
        for (var stmt : ctx.suite().statement()) {
            if (stmt instanceof MxParser.EmptyStmtContext) {
                continue;
            }
            constructor.stmtList_.add((StmtNode) visit(stmt));
        }
        return constructor;
    }

    @Override
    public ASTNode visitFuncDef(MxParser.FuncDefContext ctx) {
        FuncDefNode funcDef =
                new FuncDefNode(new Position(ctx), new Type(ctx.returntype()), ctx.Identifier(0).getText());
        for (int i = 0; i < ctx.type().size(); i++) {
            funcDef.paramList_.add(new Pair<>(new Type(ctx.type(i)), ctx.Identifier(i + 1).getText()));
        }
        for (var stmt : ctx.suite().statement()) {
            if (stmt instanceof MxParser.EmptyStmtContext) {
                continue;
            }
            funcDef.stmtList_.add((StmtNode) visit(stmt));
        }
        return funcDef;
    }

    @Override
    public ASTNode visitVarDef(MxParser.VarDefContext ctx) {
        VarDefNode varDef = new VarDefNode(new Position(ctx), new Type(ctx.type()));
        int idIndex = 0;
        for (var expr : ctx.expression()) {
            while (idIndex + 1 < ctx.Identifier().size() &&
                   ctx.Identifier(idIndex + 1).getSymbol().getTokenIndex() < expr.getStart().getTokenIndex()) {
                varDef.varList_.add(new Pair<>(ctx.Identifier(idIndex++).getText(), null));
            }
            ExprNode exprNode = (ExprNode) visit(expr);
            varDef.varList_.add(new Pair<>(ctx.Identifier(idIndex++).getText(), exprNode));
        }
        while (idIndex < ctx.Identifier().size()) {
            varDef.varList_.add(new Pair<>(ctx.Identifier(idIndex++).getText(), null));
        }
        return varDef;
    }

    @Override
    public ASTNode visitBreakStmt(MxParser.BreakStmtContext ctx) {
        return new BreakStmtNode(new Position(ctx));
    }

    @Override
    public ASTNode visitContinueStmt(MxParser.ContinueStmtContext ctx) {
        return new ContinueStmtNode(new Position(ctx));
    }

    @Override
    public ASTNode visitExprStmt(MxParser.ExprStmtContext ctx) {
        return new ExprStmtNode(new Position(ctx), (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitEmptyStmt(MxParser.EmptyStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        StmtNode init = (StmtNode) visit(ctx.initStmt);
        ExprNode cond = (ctx.condExpr == null ? null : (ExprNode) visit(ctx.condExpr));
        ExprNode step = (ctx.stepExpr == null ? null : (ExprNode) visit(ctx.stepExpr));
        StmtNode body = (StmtNode) visit(ctx.statement(1));
        ForStmtNode forStmt = new ForStmtNode(new Position(ctx), init, cond, step);
        if (body != null) {
            if (body instanceof SuiteStmtNode) {
                forStmt.body_.addAll(((SuiteStmtNode) body).stmtList_);
            } else {
                forStmt.body_.add(body);
            }
        }
        return forStmt;
    }

    @Override
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        ExprNode cond = (ExprNode) visit(ctx.expression());
        StmtNode then = (StmtNode) visit(ctx.thenStmt);
        StmtNode els = (ctx.elseStmt == null ? null : (StmtNode) visit(ctx.elseStmt));
        return new IfStmtNode(new Position(ctx), cond, then, els);
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        return new ReturnStmtNode(new Position(ctx),
                                  ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitSuiteStmt(MxParser.SuiteStmtContext ctx) {
        SuiteStmtNode suite = new SuiteStmtNode(new Position(ctx));
        for (var stmt : ctx.suite().statement()) {
            if (stmt instanceof MxParser.EmptyStmtContext) {
                continue;
            }
            suite.stmtList_.add((StmtNode) visit(stmt));
        }
        return suite;
    }

    @Override
    public ASTNode visitVarDefStmt(MxParser.VarDefStmtContext ctx) {
        return new VarDefStmtNode(new Position(ctx), (VarDefNode) visit(ctx.varDef()));
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        return new WhileStmtNode(new Position(ctx), (ExprNode) visit(ctx.expression()),
                                 (StmtNode) visit(ctx.statement()));
    }

    @Override
    public ASTNode visitArrayExpr(MxParser.ArrayExprContext ctx) {
        return new ArrayExprNode(new Position(ctx), (ExprNode) visit(ctx.expression(0)),
                                 (ExprNode) visit(ctx.expression(1)));
    }

    @Override
    public ASTNode visitAssignExpr(MxParser.AssignExprContext ctx) {
        return new AssignExprNode(new Position(ctx), (ExprNode) visit(ctx.expression(0)),
                                  (ExprNode) visit(ctx.expression(1)));
    }

    @Override
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        return new BinaryExprNode(new Position(ctx), (ExprNode) visit(ctx.expression(0)),
                                  (ExprNode) visit(ctx.expression(1)), ctx.op.getText());
    }

    @Override
    public ASTNode visitConditionalExpr(MxParser.ConditionalExprContext ctx) {
        return new ConditionalExprNode(new Position(ctx), (ExprNode) visit(ctx.expression(0)),
                                       (ExprNode) visit(ctx.expression(1)), (ExprNode) visit(ctx.expression(2)));
    }

    @Override
    public ASTNode visitFuncCallExpr(MxParser.FuncCallExprContext ctx) {
        FuncCallExprNode funcCall = new FuncCallExprNode(new Position(ctx), (ExprNode) visit(ctx.expression(0)));
        for (int i = 1; i < ctx.expression().size(); i++) {
            funcCall.args_.add((ExprNode) visit(ctx.expression(i)));
        }
        return funcCall;
    }

    @Override
    public ASTNode visitMemberExpr(MxParser.MemberExprContext ctx) {
        return new MemberExprNode(new Position(ctx), (ExprNode) visit(ctx.expression()), ctx.Identifier().getText());
    }

    @Override
    public ASTNode visitNewArrayExpr(MxParser.NewArrayExprContext ctx) {
        if (!ctx.expression().isEmpty() && ctx.arrayLiteral() != null) {
            throw new SyntaxError("Invalid New Expression Error", new Position(ctx));
        }
        Type type = new Type(ctx.typeName().getText(), ctx.LeftBracket().size());
        ArrayLiteralNode array = (ctx.arrayLiteral() == null ? null : (ArrayLiteralNode) visit(ctx.arrayLiteral()));
        NewArrayExprNode newArray = new NewArrayExprNode(new Position(ctx), type, array);
        for (int i = 0; i < ctx.expression().size(); i++) {
            if (ctx.expression(i).getStart().getTokenIndex() < ctx.LeftBracket(i).getSymbol().getTokenIndex() ||
                ctx.expression(i).getStart().getTokenIndex() > ctx.RightBracket(i).getSymbol().getTokenIndex()) {
                throw new SyntaxError("Invalid New Expression Error", new Position(ctx));
            }
            newArray.fixedSizeList_.add((ExprNode) visit(ctx.expression(i)));
        }
        return newArray;
    }

    @Override
    public ASTNode visitNewVarExpr(MxParser.NewVarExprContext ctx) {
        return new NewVarExprNode(new Position(ctx), ctx.typeName().getText());
    }

    @Override
    public ASTNode visitParenthesesExpr(MxParser.ParenthesesExprContext ctx) {
        return new ParenthesesExprNode(new Position(ctx), (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitPreUnaryExpr(MxParser.PreUnaryExprContext ctx) {
        return new PreUnaryExprNode(new Position(ctx), ctx.op.getText(), (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitUnaryExpr(MxParser.UnaryExprContext ctx) {
        return new UnaryExprNode(new Position(ctx), ctx.op.getText(), (ExprNode) visit(ctx.expression()));
    }

    @Override
    public ASTNode visitPrimary(MxParser.PrimaryContext ctx) {
        if (ctx.fstring() != null) {
            return visit(ctx.fstring());
        }
        if (ctx.Identifier() != null) {
            return new IdentifierNode(new Position(ctx), ctx.Identifier().getText());
        }
        if (ctx.literal() != null) {
            return visit(ctx.literal());
        }
        return new ThisNode(new Position(ctx));
    }

    @Override
    public ASTNode visitFstring(MxParser.FstringContext ctx) {
        FStringNode fString = new FStringNode(new Position(ctx));
        if (ctx.FString() != null) {
            fString.strList_.add(FStringNode.getFString(ctx.FString().getText(), 2, -1));
        } else {
            for (var expr : ctx.expression()) {
                fString.exprList_.add((ExprNode) visit(expr));
            }
            fString.strList_.add(FStringNode.getFString(ctx.FStringFront().getText(), 2, -1));
            for (var mid : ctx.FStringMid()) {
                fString.strList_.add(FStringNode.getFString(mid.getText(), 1, -1));
            }
            fString.strList_.add(FStringNode.getFString(ctx.FStringBack().getText(), 1, -1));
        }
        return fString;
    }

    @Override
    public ASTNode visitLiteral(MxParser.LiteralContext ctx) {
        if (ctx.True() != null) {
            return new BoolLiteralNode(new Position(ctx), true);
        }
        if (ctx.False() != null) {
            return new BoolLiteralNode(new Position(ctx), false);
        }
        if (ctx.IntegerLiteral() != null) {
            return new IntLiteralNode(new Position(ctx), Integer.parseInt(ctx.IntegerLiteral().getText()));
        }
        if (ctx.StringLiteral() != null) {
            return new StringLiteralNode(new Position(ctx), StringLiteralNode.getString(ctx.StringLiteral().getText()));
        }
        if (ctx.Null() != null) {
            return new NullLiteralNode(new Position(ctx));
        }
        return visitArrayLiteral(ctx.arrayLiteral());
    }

    @Override
    public ASTNode visitArrayLiteral(MxParser.ArrayLiteralContext ctx) {
        ArrayLiteralNode array = new ArrayLiteralNode(new Position(ctx));
        for (var literal : ctx.literal()) {
            array.elemList_.add((AtomExprNode) visit(literal));
        }
        return array;
    }
}
