package src.raiz.generated;

import src.raiz.erros.ErroLexico;
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

// Onde começou a string ou o comentário sendo lido. Se eles não fecharem, o erro só é notado no fim do arquivo, mas
// aponta para o início, que é onde está o problema
private int linhaInicio, colunaInicio;

private void erro(String mensagem) {
    throw new ErroLexico(mensagem, yyline, yycolumn, yytext());
}

private void erroDesdeOInicio(String mensagem) {
    throw new ErroLexico(mensagem, linhaInicio, colunaInicio, null);
}

private void marcaInicio() {
    linhaInicio = yyline;
    colunaInicio = yycolumn;
}

// Os inteiros têm 32 bits. Um literal maior não cabe em nenhum registrador, e o Integer.parseInt das fases
// seguintes falharia. O '-' é um operador unário à parte, então -2147483648 também é rejeitado, como em C
private Token criaInteiro() {
    try {
        Integer.parseInt(yytext());
    } catch (NumberFormatException e) {
        erro("Literal inteiro não cabe em 32 bits");
    }
    return createToken(TipoToken.INT_LITERAL);
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
NumeroFlutuanteLiteral = ((0|[1-9]\d*)\.\d+)
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
    "flut" { return createToken(TipoToken.FLUT); }
    "vazio" { return createToken(TipoToken.VAZIO); }
    "retorne" { return createToken(TipoToken.RETORNE); }
    "leia" { return createToken(TipoToken.LEIA); }
    "escreva" { return createToken(TipoToken.ESCREVA); }
    "novalinha" { return createToken(TipoToken.NOVALINHA); }
    "se" { return createToken(TipoToken.SE); }
    "entao" { return createToken(TipoToken.ENTAO); }
    "senao" { return createToken(TipoToken.SENAO); }
    "enquanto" { return createToken(TipoToken.ENQUANTO); }
    "execute" { return createToken(TipoToken.EXECUTE); }
    "para" { return createToken(TipoToken.PARA); }
    "continue" { return createToken(TipoToken.CONTINUE); }
    "pare" { return createToken(TipoToken.PARE); }
    "como" { return createToken(TipoToken.COMO); }

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
    "\"" { sbString = new StringBuilder(); marcaInicio(); yybegin(literalString); }

    /* Identificadores */
    {Identificadores} { return createToken(TipoToken.IDENTIFICADOR); }

    /* Caracteres */
    {Caractere} { return createToken(TipoToken.CARACTERE_LITERAL); }
    {CaractereMaisDeUm} { erro("Literal de caractere só pode ter um caractere"); }

    /* Números */
    {NumeroFlutuanteLiteral} { return createToken(TipoToken.FLUT_LITERAL); }
    {NumeroLiteral} { return criaInteiro(); }

    /* Comentários */
    "/*" { marcaInicio(); yybegin(comentario); }

    /* Ingnora espaço em branco */
    {EspacoEmBranco} { }

    /* Caractere inválido */
    [^] { erro("Caractere inválido"); }
}

<literalString> {
    "\""          { yybegin(YYINITIAL); return createToken(TipoToken.STRING_LITERAL, sbString.toString()); }
    {NovaLinha}   { yybegin(stringMaisDeUmaLinha); }
    [^]           { sbString.append(yytext()); } /* Consome caracteres */
    <<EOF>>       { erroDesdeOInicio("String não fechada"); }
}

<stringMaisDeUmaLinha> {
    "\""          { erroDesdeOInicio("String ocupa mais de uma linha"); }
    [^]           { } /* Consome caracteres */
    <<EOF>>       { erroDesdeOInicio("String não fechada"); }
}

<comentario> {
    "*/"    { yybegin(YYINITIAL); }
    [^]     { }
    <<EOF>> { erroDesdeOInicio("Comentário não fechado"); }
}

/* Fim do arquivo */
<<EOF>> { return createToken(TipoToken.EOF); }
