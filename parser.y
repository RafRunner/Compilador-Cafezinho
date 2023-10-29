%{
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
%}

// Terminais. Extremamente importante que tenha a mesma ordem do enum TipoToken
%token <obj> PROGRAMA CAR INT RETORNE LEIA ESCREVA NOVALINHA SE ENTAO SENAO ENQUANTO EXECUTE
%token <obj> OU E IGUAL DIFERENTE MENOR MAIOR MENOR_IGUAL MAIOR_IGUAL NEGACAO TERNARIO
%token <obj> MAIS MENOS VEZES DIVISAO RESTO
%token <obj> ATRIBUICAO VIRGULA PONTO_E_VIRGULA DOIS_PONTOS ABRE_CHAVE FECHA_CHAVE ABRE_PARENTESES
%token <obj> FECHA_PARENTESES ABRE_COLCHETE FECHA_COLCHETE
%token <obj> LITERAL_STRING IDENTIFICADOR INT_LITERAL

// Não terminais
%type <obj> Tipo

%%

Programa:
    DeclFuncVar DeclProg {
        debugar("Programa derivado com sucesso");
    }
  ;

DeclFuncVar:
      Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar {
        debugar("Declaração de variáveis do tipo " + getLexema($1)+ " começando com variável " + getLexema($2));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar { 
        debugar("Declaração de variáveis do tipo " + getLexema($1) + " começando com variável vetor " + getLexema($2) + " tamanho: " + getLexema($4));
    }
    | Tipo IDENTIFICADOR DeclFunc DeclFuncVar {
        debugar("Declaracao de função " + getLexema($1) + " " + getLexema($2));
    }
    | /* vazio */
    ;

DeclProg:
    PROGRAMA Bloco { debugar("Bloco do programa derivado"); }
    ;

DeclVar:
      VIRGULA IDENTIFICADOR DeclVar {
        debugar("Declaração de variável " + getLexema($2));
    }
    | VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar {
        debugar("Declaração de variável vetor " + getLexema($2) + " tamanho: " + getLexema($4));
        }
    | /* vazio */
    ;

DeclFunc:
     ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco { debugar("Declaraco de função derivado"); }
     ;

ListaParametros:
      /* vazio */ { debugar("Lista de parâmetros finalizada"); }
    | ListaParametrosCont  { debugar("Mais um parâmetro"); }
    ;

ListaParametrosCont:
      Tipo IDENTIFICADOR { debugar("Parâmetro " + getLexema($2) + " declarado"); }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE { debugar("Parâmetro vetor " + getLexema($2) + " declarado, que não é o último"); }
    | Tipo IDENTIFICADOR, ListaParametrosCont { debugar("Parâmetro " + getLexema($2) + " declarado"); }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont  { debugar("Parâmetro vetor " + getLexema($2) + " declarado, que não é o último"); }
    ;

Bloco:
      ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE { debugar("Bloco com comandos derivado"); }
    | ABRE_CHAVE ListaDeclVar FECHA_CHAVE { debugar("Bloco somente com variáveis derivado"); }
    ;

ListaDeclVar:
      /* vazio */ { debugar("Declaração de variáveis em bloco finalizada"); }
    | Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar {
        debugar("Declaração de variáveis em bloco do tipo " + getLexema($1)+ " começando com variável " + getLexema($2));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar {
        debugar("Declaração de variáveis em bloco do tipo " + getLexema($1)+ " começando com variável vetor " + getLexema($2) + " tamanho: " + getLexema($4));
    }
    ;

Tipo:
      INT { debugar("Tipo: " + getLexema($1)); $$ = $1; }
    | CAR { debugar("Tipo: " + getLexema($1)); $$ = $1; }
    ;

ListaComando:
      Comando { debugar("Último comando derivado"); }
    | Comando ListaComando  { debugar("Comando derivado"); }
    ;

Comando:
      PONTO_E_VIRGULA { debugar("Comando vazio ';'"); }
    | Expr PONTO_E_VIRGULA { debugar("Comando com expressão"); }
    | RETORNE Expr PONTO_E_VIRGULA { debugar("Comando de retorno"); }
    | LEIA LValueExpr PONTO_E_VIRGULA { debugar("Comando leia"); }
    | ESCREVA Expr PONTO_E_VIRGULA { debugar("Comando escreva"); }
    | ESCREVA LITERAL_STRING PONTO_E_VIRGULA { debugar("Comando escreva String " + getLexema($2)); }
    | NOVALINHA PONTO_E_VIRGULA { debugar("Comando novalinha"); }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando { debugar("Comando se simples"); }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando { debugar("Comando se senao"); }
    | ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando { debugar("Comando enquanto"); }
    | Bloco { debugar("Comando bloco"); }
    ;

Expr:
    AssignExpr { debugar("Expr derivada"); }
    ;

AssignExpr:
      CondExpr { debugar("Expressão CondExpr derivada"); }
    | LValueExpr ATRIBUICAO AssignExpr { debugar("Expressão de atribuição derivada"); }
    ;

CondExpr:
      OrExpr { debugar("Expressão OrExpr derivada"); }
    | OrExpr TERNARIO Expr DOIS_PONTOS CondExpr { debugar("Expressão ou ternária derivada"); }
    ;

OrExpr:
      OrExpr OU AndExpr { debugar("Expressão ou derivada"); }
    | AndExpr { debugar("Expressão e derivada"); }
    ;

AndExpr:
      AndExpr E EqExpr { debugar("Expressão e derivada"); }
    | EqExpr { debugar("Expressão EqExpr derivada"); }
    ;

EqExpr:
      EqExpr IGUAL DesigExpr { debugar("Expressão igual derivada"); }
    | EqExpr DIFERENTE DesigExpr { debugar("Expressão diferente derivada"); }
    | DesigExpr { debugar("Expressão DesigExpr derivada"); }
    ;

DesigExpr:
      DesigExpr MENOR AddExpr { debugar("Expressão menor derivada"); }
    | DesigExpr MAIOR AddExpr { debugar("Expressão maior derivada"); }
    | DesigExpr MAIOR_IGUAL AddExpr { debugar("Expressão maior igual derivada"); }
    | DesigExpr MENOR_IGUAL AddExpr { debugar("Expressão menor igual derivada"); }
    | AddExpr { debugar("Expressão AddExpr derivada"); }
    ;

AddExpr:
      AddExpr MAIS MulExpr { debugar("Expressão mais derivada"); }
    | AddExpr MENOS MulExpr { debugar("Expressão menos derivada"); }
    | MulExpr { debugar("Expressão MulExpr derivada"); }
    ;

MulExpr:
      MulExpr VEZES UnExpr { debugar("Expressão vezes derivada"); }
    | MulExpr DIVISAO UnExpr { debugar("Expressão divisão derivada"); }
    | MulExpr RESTO UnExpr { debugar("Expressão resto derivada"); }
    | UnExpr { debugar("Expressão UnExpr derivada"); }
    ;

UnExpr:
      MENOS PrimExpr { debugar("Expressão menos unária derivada"); }
    | NEGACAO PrimExpr { debugar("Expressão negação derivada"); }
    | PrimExpr { debugar("Expressão PrimExpr derivada"); }
    ;

LValueExpr:
      IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE { debugar("Expressão de indexação no vetor " + getLexema($1)); }
    | IDENTIFICADOR { debugar("Expressão de identificador(LValueExpr) " + getLexema($1)); }
    ;

PrimExpr:
      IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES { debugar("Expressão de chamada da função " + getLexema($1)); }
    | IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES { debugar("Expressão de chamada da função sem argumentos " + getLexema($1)); }
    | IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE { debugar("Expressão de indexação no vetor " + getLexema($1)); }
    | IDENTIFICADOR { debugar("Expressão de identificador(PrimExpr) " + getLexema($1)); }
    | LITERAL_STRING { debugar("Literal string/caractere " + getLexema($1)); }
    | INT_LITERAL { debugar("Literal inteiro " + getLexema($1)); }
    | ABRE_PARENTESES Expr FECHA_PARENTESES { debugar("Expressão entre parênteses derivada"); }
    ;

ListExpr:
    AssignExpr { debugar("Lista de expressões finalizada"); }
    | ListExpr VIRGULA AssignExpr { debugar("Mais um parâmetro em lista de expressão declarado"); }
    ;

%%

private Lexer lexer;
private boolean debugInterno = false;

private void debugar(String string) {
    if (debugInterno) {
        System.out.println(string);
    }
}

private String getLexema(Object token) {
    return ((Token) token).getLexema();
}

private String montaMensagemErro(String mensagemErro, Token t) {
      if (t != null) {
        return "ERRO: " + mensagemErro + "; linha: " + (t.getLinha() + 1) + ", coluna: " + (t.getColuna() + 1) + ", próximo de " + t.getLexema();
    } else {
        return "ERRO: " + mensagemErro;
    }  
}

private void yyerror(String mensagemErro) {
    throw new RuntimeException(montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.getLexema());
}

// Função que traduz o Token do lexer para um valor int do BYACC/J
private int yylex() {
    try {
        Token proximoToken = lexer.yylex();
        debugar(proximoToken.toString());
        if (proximoToken != null && proximoToken.getTipo() != TipoToken.EOF) {
            yylval = new ParserVal(proximoToken);

            // Os valores de 0 a 256 são reservados, por isso adicionamos 257
            return proximoToken.getTipo().ordinal() + 257;
        } else {
            return 0; // EOF
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

public void analisar(Reader leitor) {
    analisar(leitor, false);
}

public void analisar(Reader leitor, boolean debugInterno) {
    lexer = new Lexer(leitor);
    this.debugInterno = debugInterno;
    yyparse();
}
