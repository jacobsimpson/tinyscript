grammar TinyScript;

options
{
    language = Java;
}

@header {
    package jacob.simpson.tinyscript.grammar;
}

program
    :   printStatement*
    ;

printStatement
    :   PRINT
    |   PRINT STRING
    ;

PRINT
    :   'print'
    ;

STRING
    : '"' ~('\"')* '"' {setText(getText().substring(1, getText().length()-1));}
    ;

WS  :   [ \t\r\n]+ -> skip
    ;
