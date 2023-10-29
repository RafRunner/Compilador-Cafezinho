package src.raiz.generated;

import src.raiz.token.*;
import java.io.*;

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

private void erro(String msg) {
    throw new RuntimeException("ERRO: " + msg + ". LINHA: " + (yyline + 1) + ", COLUNA " + (yycolumn + 1));
}

private Token createToken(TipoToken tipo) {
    return new Token(tipo, yytext(), yyline, yycolumn);
}

// Método auxiliar para ignorar comentários
private void ignoraComentario() throws IOException {
    while (true) {
        if (yytext().equals("*/")) {
            return;
        } else if (yytext().equals("")) {
            erro("COMENTÁRIO NÃO TERMINA");
        }
        yypushback(1);
        yylex();
    }
}

%}

EscapesString = \\\\([tnr\"\\\\])
Identificadores = [_a-zA-Z][_a-zA-Z0-9]*
NumeroLiteral = 0|[1-9]\d*
EspacoEmBranco = [ \t\n\r]+

%%

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
\"[^\"\n]*\n { erro("CADEIA DE CARACTERES OCUPA MAIS DE UMA LINHA"); }
\"[^\"]*$ { erro("CADEIA DE CARACTERES NÃO FECHADA"); }
\"(EscapesString|[^\"\n])*\" { return createToken(TipoToken.LITERAL_STRING); }

/* Identificadores */
{Identificadores} { return createToken(TipoToken.IDENTIFICADOR); }

/* Números */
{NumeroLiteral} { return createToken(TipoToken.INT_LITERAL); }

/* Ignora comentários */
"/*" { ignoraComentario(); }

/* Ingnora espaço em branco */
{EspacoEmBranco} { }

/* Caractere inválido */
[^] { erro("CARACTERE INVÁLIDO"); }

/* Fim do arquivo */
<<EOF>> { return createToken(TipoToken.EOF); }
