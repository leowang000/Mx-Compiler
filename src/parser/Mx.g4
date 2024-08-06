grammar Mx;

@header {package parser;}

program : (funcDef | (classDef Semicolon) | (varDef Semicolon))*;

BaseTypeName : Bool | Int | String;
TypeName : BaseTypeName | Identifier;
Type : TypeName (LeftBracket RightBracket)*;
varDef : Type Identifier (Assign expression)? (Comma Identifier (Assign expression)?)*;

ReturnType : Type | Void;
funcDef : ReturnType Identifier LeftParentheses Type Identifier (Comma Type Identifier)* RightParentheses suite;

constructorDef : Identifier LeftParentheses RightParentheses suite;
classDef : Class Identifier LeftBrace ((varDef Semicolon) | funcDef | constructorDef)* RightBrace;

suite : LeftBrace statement* RightBrace;

statement
    : suite #suiteStmt
    | varDef Semicolon #varDefStmt
    | If LeftParentheses expression RightParentheses thenStmt = statement (elseStmt = statement)? #ifStmt
    | While LeftParentheses expression RightParentheses statement #whileStmt
    | For LeftParentheses initStmt = statement Semicolon condExpr = expression Semicolon stepExpr = expression RightParentheses statement #forStmt
    | Return expression? Semicolon #returnStmt
    | Break Semicolon #breakStmt
    | Continue Semicolon #continueStmt
    | expression Semicolon #exprStmt
    | Semicolon #emptyStmt
    ;

expression
    : fstring #fstringExpr
    | New TypeName (LeftParentheses RightParentheses)? #newVarExpr
    | New TypeName (LeftBracket expression? RightBracket)+ ArrayLiteral? #newArrayExpr
    | expression op = (Increment | Decrement) #unaryExpr
    | expression LeftParentheses expression (Comma expression)* RightParentheses #funcCallExpr
    | expression LeftBracket expression RightBracket #arrayExpr
    | expression op = Member Identifier #memberExpr
    | op = (Increment | Decrement) expression #preUnaryExpr
    | op = (Plus | Minus | Not | LogicalNot) expression #unaryExpr
    | expression op = (Mul | Div | Mod) expression #binaryExpr
    | expression op = (Plus | Minus) expression #binaryExpr
    | expression op = (LeftShift | RightShift) expression #binaryExpr
    | expression op = (Greater | GreaterEqual | LeftShift | LessEqual) expression #binaryExpr
    | expression op = (Equal | NotEqual) expression #binaryExpr
    | expression op = And expression #binaryExpr
    | expression op = Xor expression #binaryExpr
    | expression op = Or expression #binaryExpr
    | expression op = LogicalAnd expression #binaryExpr
    | expression op = LogicalOr expression #binaryExpr
    | <assoc = right> expression Question expression Colon expression #conditionalExpr
    | <assoc = right> expression op = Assign expression #assignExpr
    | LeftParentheses expression RightParentheses #parenthesesExpr
    | primary #atomExpr
    ;

VisibleChar : [\u0020-\u007E];
FStringChar : Escape | '$$' | VisibleChar~[$\\"];
fstring : 'f' Quote FStringChar*? '$' expression '$' FStringChar*? Quote;

primary
    : Literal
    | Identifier
    | This
    ;

Literal
    : True
    | False
    | IntegerLiteral
    | StringLiteral
    | Null
    | ArrayLiteral
    ;
ArrayLiteral : LeftBrace Literal (Comma Literal)* RightBrace;

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
Escape : '\\\\' | '\\n' | '\\"';
StringLiteral : Quote (Escape | VisibleChar~[\\"])*? Quote;

Identifier : [a-zA-Z][a-zA-Z0-9_]*;

LineComment : '//' ~[\r\n]* -> skip;
BlockComment : '/*' .*? '*/' -> skip;
WhiteSpace : [ \t]+ -> skip;
NewLine : ('\r' '\n'? | '\n') -> skip;