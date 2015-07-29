grammar TinyScript;

options
{
    language = Java;
}

@header {
    package jacob.simpson.tinyscript.grammar;
}

program
    :   ( printStatement | declarationStatement | assignmentStatement )*
    ;

printStatement
    :   PRINT
    |   PRINT STRING
    |   PRINT IDENTIFIER
    ;

declarationStatement
    :   VAR IDENTIFIER
    ;

assignmentStatement
    :   IDENTIFIER '=' INTEGER
    ;

PRINT
    :   'print'
    ;

VAR
    :   'var'
    ;

STRING
    : '"' ~('\"')* '"' {setText(getText().substring(1, getText().length()-1));}
    ;

IDENTIFIER
    :   LETTER (DIGIT | LETTER)*
    ;

INTEGER
    :   DIGIT+
    ;

WS  :   [ \t\r\n]+ -> skip
    ;

fragment DIGIT         : '0' .. '9';
fragment LETTER        : 'a' .. 'z' | 'A' .. 'Z';