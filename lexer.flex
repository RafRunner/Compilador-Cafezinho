package src.raiz.generated;

import src.raiz.token.*;

%%

%public
%class Lexer

%byaccj
%unicode
%type Token
%function yylex
%column
%line

%{

private StringBuilder sbString;

private void erro(String msg) {
    throw new RuntimeException("ERRO: " + msg + ". LINHA: " + (yyline + 1) + ", COLUNA " + (yycolumn + 1));
}

private Token createToken(TipoToken tipo, String lexema) {
    return new Token(tipo, lexema, yyline, yycolumn);
}

private Token createToken(TipoToken tipo) {
    return createToken(tipo, yytext());
}

%}

NovaLinha = \n|\r|\r\n

Caractere = '[^\']'
CaractereMaisDeUm = '.[^']

Identificadores = [_a-zA-Z][_a-zA-Z0-9]*
NumeroLiteral = 0|[1-9]\d*
EspacoEmBranco = [ \t\n\r]+

%state literalString
%state stringMaisDeUmaLinha
%state comentario

%%

<YYINITIAL> {
    /* Palavras reservadas */
    "programa" { return createToken(TipoToken.PROGRAMA); }
    "car" { return createToken(TipoToken.CAR); }
    "int" { return createToken(TipoToken.INT); }
    "retorne" { return createToken(TipoToken.RETORNE); }
    "leia" { return createToken(TipoToken.LEIA); }
    "escreva" { return createToken(TipoToken.ESCREVA); }
    "novalinha" { return createToken(TipoToken.NOVALINHA); }
    "se" { return createToken(TipoToken.SE); }
    "entao" { return createToken(TipoToken.ENTAO); }
    "senao" { return createToken(TipoToken.SENAO); }
    "enquanto" { return createToken(TipoToken.ENQUANTO); }
    "execute" { return createToken(TipoToken.EXECUTE); }

    /* Operadores lógicos */
    "ou" { return createToken(TipoToken.OU); }
    "e" { return createToken(TipoToken.E); }
    "==" { return createToken(TipoToken.IGUAL); }
    "!=" { return createToken(TipoToken.DIFERENTE); }
    "<" { return createToken(TipoToken.MENOR); }
    ">" { return createToken(TipoToken.MAIOR); }
    "<=" { return createToken(TipoToken.MENOR_IGUAL); }
    ">=" { return createToken(TipoToken.MAIOR_IGUAL); }
    "!" { return createToken(TipoToken.NEGACAO); }
    "?" { return createToken(TipoToken.TERNARIO); }

    /* Operadores matemáticos */
    "+" { return createToken(TipoToken.MAIS); }
    "-" { return createToken(TipoToken.MENOS); }
    "*" { return createToken(TipoToken.VEZES); }
    "/" { return createToken(TipoToken.DIVISAO); }
    "%" { return createToken(TipoToken.RESTO); }

    /* Outros */
    "=" { return createToken(TipoToken.ATRIBUICAO); }
    "," { return createToken(TipoToken.VIRGULA); }
    ";" { return createToken(TipoToken.PONTO_E_VIRGULA); }
    ":" { return createToken(TipoToken.DOIS_PONTOS); }
    "{" { return createToken(TipoToken.ABRE_CHAVE); }
    "}" { return createToken(TipoToken.FECHA_CHAVE); }
    "(" { return createToken(TipoToken.ABRE_PARENTESES); }
    ")" { return createToken(TipoToken.FECHA_PARENTESES); }
    "[" { return createToken(TipoToken.ABRE_COLCHETE); }
    "]" { return createToken(TipoToken.FECHA_COLCHETE); }

    /* Strings */
    "\"" { sbString = new StringBuilder(); yybegin(literalString); }

    /* Identificadores */
    {Identificadores} { return createToken(TipoToken.IDENTIFICADOR); }

    /* Caracteres */
    {Caractere} { return createToken(TipoToken.CARACTERE_LITERAL); }
    {CaractereMaisDeUm} { erro("LITERAL DE CARACTERE SÓ PODE TER UM VALOR: " + yytext()); }

    /* Identificadores */
    {Identificadores} { return createToken(TipoToken.IDENTIFICADOR); }

    /* Números */
    {NumeroLiteral} { return createToken(TipoToken.INT_LITERAL); }

    /* Comentários */
    "/*" { yybegin(comentario); }

    /* Ingnora espaço em branco */
    {EspacoEmBranco} { }

    /* Caractere inválido */
    [^] { erro("CARACTERE INVÁLIDO"); }
}

<literalString> {
    "\""          { yybegin(YYINITIAL); return createToken(TipoToken.STRING_LITERAL, sbString.toString()); }
    {NovaLinha}   { yybegin(stringMaisDeUmaLinha); }
    [^]           { sbString.append(yytext()); } /* Consome caracteres */
    <<EOF>>       { erro("CADEIA DE CARACTERES NÃO FECHADA"); }
}

<stringMaisDeUmaLinha> {
    "\""          { erro("CADEIA DE CARACTERES OCUPA MAIS DE UMA LINHA"); }
    [^]           { } /* Consome caracteres */
    <<EOF>>       { erro("CADEIA DE CARACTERES NÃO FECHADA"); } 
}

<comentario> {
    "*/"    { yybegin(YYINITIAL); }
    [^]     { }
    <<EOF>> { erro("COMENTÁRIO NÃO TERMINA"); }
}

/* Fim do arquivo */
<<EOF>> { return createToken(TipoToken.EOF); }
