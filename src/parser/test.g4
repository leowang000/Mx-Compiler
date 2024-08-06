grammar test;

literal : StringLiteral;

Identifier : [a-zA-Z][a-zA-Z0-9_]*;

Quote: '"';
Escape : '\\\\' | '\\n' | '\\"';
fragment Char : Escape | [\u0020-\u0021\u0023-\u005B\u005D-\u007E];
StringLiteral : Quote Char* Quote;

WhiteSpace : [ \t]+ -> skip;