%{
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
%}

// Terminais. Extremamente importante que tenha a mesma ordem do enum TipoToken
%token <obj> PROGRAMA CAR INT RETORNE LEIA ESCREVA NOVALINHA SE ENTAO SENAO ENQUANTO EXECUTE
%token <obj> OU E IGUAL DIFERENTE MENOR MAIOR MENOR_IGUAL MAIOR_IGUAL NEGACAO TERNARIO
%token <obj> MAIS MENOS VEZES DIVISAO RESTO
%token <obj> ATRIBUICAO VIRGULA PONTO_E_VIRGULA DOIS_PONTOS ABRE_CHAVE FECHA_CHAVE ABRE_PARENTESES
%token <obj> FECHA_PARENTESES ABRE_COLCHETE FECHA_COLCHETE
%token <obj> STRING_LITERAL CARACTERE_LITERAL IDENTIFICADOR INT_LITERAL

// Não terminais
%type <obj> DeclFuncVar DeclVar Tipo

%%

Programa:
    DeclFuncVar { 
        for (Declaracao declaracao : ((List<Declaracao>) $1)) {
            this.programa.addDeclaracao(declaracao);
        }
    }
  ;

DeclFuncVar:
      Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;
        List<Variavel> resto = (List<Variavel>) $3;
        List<Declaracao> outrasDeclaracoes = (List<Declaracao>) $5;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.add(0, montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar { 
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) $4, (Token) $2);
        List<Variavel> resto = (List<Variavel>) $6;
        List<Declaracao> outrasDeclaracoes = (List<Declaracao>) $8;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.add(0, montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | { $$ = new ArrayList<Declaracao>(); }
    ;

DeclVar:
      VIRGULA IDENTIFICADOR DeclVar {
        List<Variavel> variaveis = (List<Variavel>) $3;
        Variavel variavel = new Variavel(null, (Token) $2, null);
        variaveis.add(0, variavel);

        $$ = variaveis;
    }
    | VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar {
        List<Variavel> variaveis = (List<Variavel>) $6;
        Variavel variavel = new Variavel(null, (Token) $2, tokenParaTamanhoVetor((Token) $4, (Token) $2));
        variaveis.add(0, variavel);

        $$ = variaveis;
        }
    | { $$ = new ArrayList<Variavel>(); }
    ;

Tipo:
      INT { $$ = new TipoVariavelNo((Token) $1, TipoVariavel.INTEIRO); }
    | CAR { $$ = new TipoVariavelNo((Token) $1, TipoVariavel.CARACTERE); }
    ;

%%

private Lexer lexer;
private Programa programa;
private boolean debugInterno = false;

private void debugar(String string) {
    if (debugInterno) {
        System.out.println(string);
    }
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Variavel> resto) {
    List<Variavel> variaveis = new ArrayList<Variavel>();
    variaveis.add(variavel);
    variaveis.addAll(resto);
    for (Variavel v : resto) {
        v.setTipo(variavel.getTipo());
    }

    DeclaracaoDeVariavel declaracao = new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);
    debugar(declaracao.toString());

    return declaracao;
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

private void reportaErroSemantico(String mensagemErro, Token t) {
    programa.reportaErroSemantico(montaMensagemErro(mensagemErro, t));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.getLexema());
}

private Integer tokenParaTamanhoVetor(Token numero, Token variavel) {
    Integer tamanho = tokenParaInt(numero);
    if (tamanho == 0) {
        reportaErroSemantico("Array " + variavel.getLexema() + " não pode ter tamanho 0", variavel);
    }

    return tamanho;
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

public Programa getPrograma() {
    return programa;
}

public void analisar(Reader leitor) {
    analisar(leitor, false);
}

public void analisar(Reader leitor, boolean debugInterno) {
    programa = new Programa();
    lexer = new Lexer(leitor);
    this.debugInterno = debugInterno;
    yyparse();

    if (!programa.getErrosSemanticos().isEmpty()) {
        StringBuilder sb = new StringBuilder("Erros semânticos foram identificados na análise:\n");

        for (String mensagem : programa.getErrosSemanticos()) {
            sb.append(mensagem).append("\n");
        }

        throw new RuntimeException(sb.toString());
    }
}
