grammar Mx;

@header {package parser;}

program : (funcDef | (classDef Semicolon) | (varDef Semicolon))* EOF;

basetypeName : Bool | Int | String;
typeName : basetypeName | Identifier;
type : typeName (LeftBracket RightBracket)*;
varDef : type Identifier (Assign expression)? (Comma Identifier (Assign expression)?)*;

returntype : type | Void;
funcDef : returntype Identifier LeftParentheses (type Identifier (Comma type Identifier)*)? RightParentheses suite;

constructorDef : Identifier LeftParentheses RightParentheses suite;
classDef : Class Identifier LeftBrace ((varDef Semicolon) | funcDef | constructorDef)* RightBrace;

suite : LeftBrace statement* RightBrace;

statement
    : suite #suiteStmt
    | varDef Semicolon #varDefStmt
    | If LeftParentheses expression RightParentheses thenStmt = statement (Else elseStmt = statement)? #ifStmt
    | While LeftParentheses expression RightParentheses statement #whileStmt
    | For LeftParentheses initStmt = statement condExpr = expression Semicolon stepExpr = expression RightParentheses statement #forStmt
    | Return expression? Semicolon #returnStmt
    | Break Semicolon #breakStmt
    | Continue Semicolon #continueStmt
    | expression Semicolon #exprStmt
    | Semicolon #emptyStmt
    ;

expression
    : LeftParentheses expression RightParentheses #parenthesesExpr
    | New typeName (LeftParentheses RightParentheses)? #newVarExpr
    | New typeName (LeftBracket expression? RightBracket)+ arrayLiteral? #newArrayExpr
    | expression op = (Increment | Decrement) #unaryExpr
    | expression LeftParentheses (expression (Comma expression)*)? RightParentheses #funcCallExpr
    | expression LeftBracket expression RightBracket #arrayExpr
    | expression op = Member Identifier #memberExpr
    | op = (Increment | Decrement) expression #preUnaryExpr
    | op = (Plus | Minus | Not | LogicalNot) expression #unaryExpr
    | expression op = (Mul | Div | Mod) expression #binaryExpr
    | expression op = (Plus | Minus) expression #binaryExpr
    | expression op = (LeftShift | RightShift) expression #binaryExpr
    | expression op = (Greater | GreaterEqual | Less | LessEqual) expression #binaryExpr
    | expression op = (Equal | NotEqual) expression #binaryExpr
    | expression op = And expression #binaryExpr
    | expression op = Xor expression #binaryExpr
    | expression op = Or expression #binaryExpr
    | expression op = LogicalAnd expression #binaryExpr
    | expression op = LogicalOr expression #binaryExpr
    | <assoc = right> expression Question expression Colon expression #conditionalExpr
    | <assoc = right> expression op = Assign expression #assignExpr
    | primary #atomExpr
    ;

primary
    : fstring
    | Identifier
    | literal
    | This
    ;

fstring
    : Fstring
    | FStringFront expression (FStringMid expression)* FStringBack
    ;

arrayLiteral : LeftBrace literal (Comma literal)* RightBrace;
literal
    : True
    | False
    | IntegerLiteral
    | StringLiteral
    | Null
    | arrayLiteral
    ;

Void : 'void';
Bool : 'bool';
Int : 'int';
String : 'string';
New : 'new';
Class : 'class';
Null : 'null';
True : 'true';
False : 'false';
This : 'this';
If : 'if';
Else : 'else';
For : 'for';
While : 'while';
Break : 'break';
Continue : 'continue';
Return : 'return';

Identifier : [a-zA-Z][a-zA-Z0-9_]*;

Plus : '+';
Minus : '-';
Mul : '*';
Div : '/';
Mod : '%';
Greater : '>';
Less : '<';
GreaterEqual : '>=';
LessEqual : '<=';
NotEqual : '!=';
Equal : '==';
LogicalAnd : '&&';
LogicalOr : '||';
LogicalNot : '!';
LeftShift : '<<';
RightShift : '>>';
And : '&';
Or : '|';
Xor : '^';
Not : '~';
Assign : '=';
Increment : '++';
Decrement : '--';
Member : '.';
LeftParentheses : '(';
RightParentheses : ')';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';
Question : '?';
Colon : ':';
Semicolon : ';';
Quote : '"';
Comma : ',';

IntegerLiteral : '0' | [1-9][0-9]*;
fragment Escape : '\\\\' | '\\n' | '\\"';
fragment Char : Escape | [\u0020-\u0021\u0023-\u005B\u005D-\u007E];
StringLiteral : Quote Char* Quote;
fragment FStringChar : Escape | '$$' | [\u0020-\u0021\u0023\u0025-\u005B\u005D-\u007E];
Fstring : 'f' Quote FStringChar* Quote;
FStringFront : 'f' Quote FStringChar* '$';
FStringMid : '$' FStringChar* '$';
FStringBack : '$' FStringChar* Quote;

LineComment : '//' ~[\r\n]* -> skip;
BlockComment : '/*' .*? '*/' -> skip;
WhiteSpace : [ \t]+ -> skip;
NewLine : ('\r' '\n'? | '\n') -> skip;