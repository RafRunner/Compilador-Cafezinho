%{
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
%}

// Terminais. Extremamente importante que tenha a mesma ordem do enum TipoToken
%token <obj> PROGRAMA CAR INT RETORNE LEIA ESCREVA NOVALINHA SE ENTAO SENAO ENQUANTO EXECUTE
%token <obj> OU E IGUAL DIFERENTE MENOR MAIOR MENOR_IGUAL MAIOR_IGUAL NEGACAO TERNARIO
%token <obj> MAIS MENOS VEZES DIVISAO RESTO
%token <obj> ATRIBUICAO VIRGULA PONTO_E_VIRGULA DOIS_PONTOS ABRE_CHAVE FECHA_CHAVE ABRE_PARENTESES
%token <obj> FECHA_PARENTESES ABRE_COLCHETE FECHA_COLCHETE
%token <obj> STRING_LITERAL CARACTERE_LITERAL IDENTIFICADOR INT_LITERAL

// Não terminais
%type <obj> DeclFuncVar DeclVar Tipo Bloco ListaDeclVar, DeclProg, DeclFunc, ListaParametros
%type <obj> ListaComando, Comando

%%

Programa:
    DeclFuncVar DeclProg {
        debugar("Programa derivado com sucesso");
        this.programa.getDeclaracoes().add(0, (DeclaracaoFuncoesEVariaveis) $1);
        this.programa.getDeclaracoes().add((BlocoPrograma) $2);
    }
  ;

DeclFuncVar:
      Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável " + identificador.getLexema());

        List<Variavel> resto = (List<Variavel>) $3;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $5;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) $4, (Token) $2);

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável vetor "
            + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        List<Variavel> resto = (List<Variavel>) $6;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $8;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR DeclFunc DeclFuncVar {
        List<ParametroFuncao> parametros = (List<ParametroFuncao>) ((Object[]) $3)[0];
        BlocoDeclaracoes corpo = (BlocoDeclaracoes) ((Object[]) $3)[1];

        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao((Token) $2, (TipoVariavelNo) $1, corpo, parametros);
        debugar("Declaracao de função " + declaracaoFuncao.getNome() + " " + declaracaoFuncao.getTipoRetorno());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $4;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().add(0, declaracaoFuncao);

        $$ = outrasDeclaracoes;
    }
    | { $$ = new DeclaracaoFuncoesEVariaveis(); }
    ;

DeclProg:
    PROGRAMA Bloco {
        debugar("Bloco do programa derivado");
        $$ = new BlocoPrograma((Token) $1, (BlocoDeclaracoes) $2);
    }
    ;

DeclVar:
      VIRGULA IDENTIFICADOR DeclVar {
        Token identificador = (Token) $2;
        List<Variavel> variaveis = (List<Variavel>) $3;

        debugar("Declaração de variável " + identificador.getLexema());

        Variavel variavel = new Variavel(null, identificador, null);
        variaveis.add(0, variavel);

        $$ = variaveis;
    }
    | VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar {
        Token identificador = (Token) $2;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) $4, identificador);
        List<Variavel> variaveis = (List<Variavel>) $6;

        debugar("Declaração de variável vetor " + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        Variavel variavel = new Variavel(null, (Token) $2, tamanhoVetor);
        variaveis.add(0, variavel);

        $$ = variaveis;
    }
    | { $$ = new LinkedList<Variavel>(); }
    ;

DeclFunc:
     ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco {
        debugar("Declaraco de função derivado");
        $$ = new Object[]{$2, $4};
     }
     ;

ListaParametros:
      /* vazio */ {
        debugar("Lista de parâmetros vazia.");
        $$ = new LinkedList<>();
    }
    | ListaParametrosCont {
        debugar("Lista de parâmetros finalizada. Tamanho: " + parametrosAtuais.size());
        $$ = new LinkedList<>(parametrosAtuais);
        parametrosAtuais = new LinkedList<>();
    }
    ;

ListaParametrosCont:
      Tipo IDENTIFICADOR {
        debugar("Útimo parâmetro " + getLexema($2) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, false));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE {
        debugar("Útimo parâmetro vetor " + getLexema($2) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, true));
    }
    | Tipo IDENTIFICADOR VIRGULA ListaParametrosCont {
        debugar("Parâmetro " + getLexema($2) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, false));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont {
        debugar("Parâmetro vetor " + getLexema($2) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, true));
    }
    ;

Bloco:
      ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE {
        debugar("Bloco com comandos derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) $1);
        bloco.getDeclaracaoes().add((Declaracao) $2);
        bloco.getDeclaracaoes().addAll((List<Declaracao>) $3);

        $$ = bloco;
    }
    | ABRE_CHAVE ListaDeclVar FECHA_CHAVE { 
        debugar("Bloco somente com variáveis derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) $1);
        bloco.getDeclaracaoes().add((Declaracao) $2);

        $$ = bloco;
    }
    ;

ListaDeclVar:
      /* vazio */ {
        $$ = new DeclaracaoVariavelEmBloco();
      }
    | Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar {
        Variavel variavel = new Variavel((TipoVariavelNo) $1, (Token) $2, null);
        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável " + variavel.getNome());

        List<Variavel> outrasVariaveis = (List<Variavel>) $3;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) $5;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar {
        Variavel variavel = new Variavel((TipoVariavelNo) $1, (Token) $2, null);

        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável vetor "
            + variavel.getNome() + " tamanho: " + variavel.getTamanhoVetor());

        List<Variavel> outrasVariaveis = (List<Variavel>) $3;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) $8;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    ;

Tipo:
      INT { debugar("Tipo: " + getLexema($1)); $$ = new TipoVariavelNo((Token) $1, TipoVariavel.INTEIRO); }
    | CAR { debugar("Tipo: " + getLexema($1)); $$ = new TipoVariavelNo((Token) $1, TipoVariavel.CARACTERE); }
    ;

ListaComando:
      Comando {
        debugar("Primeiro comando derivado");
        List<Comando> comandos = new LinkedList<>();
        // comandos.add(0, (Comando) $1);
        $$ = comandos;
    }
    | Comando ListaComando  {
        debugar("Comando derivado");
        List<Comando> comandos = (List<Comando>) $2;
        // comandos.add(0, (Comando) $1);
        $$ = comandos;
    }
    ;

Comando:
      PONTO_E_VIRGULA { debugar("Comando vazio ';'"); }
    | Expr PONTO_E_VIRGULA { debugar("Comando com expressão"); }
    | RETORNE Expr PONTO_E_VIRGULA { debugar("Comando de retorno"); }
    | LEIA LValueExpr PONTO_E_VIRGULA { debugar("Comando leia"); }
    | ESCREVA Expr PONTO_E_VIRGULA { debugar("Comando escreva"); }
    | ESCREVA STRING_LITERAL PONTO_E_VIRGULA { debugar("Comando escreva String " + getLexema($2)); }
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
    | STRING_LITERAL { debugar("Literal string " + getLexema($1)); }
    | CARACTERE_LITERAL { debugar("Literal caractere " + getLexema($1)); }
    | INT_LITERAL { debugar("Literal inteiro " + getLexema($1)); }
    | ABRE_PARENTESES Expr FECHA_PARENTESES { debugar("Expressão entre parênteses derivada"); }
    ;

ListExpr:
    AssignExpr { debugar("Lista de expressões finalizada"); }
    | ListExpr VIRGULA AssignExpr { debugar("Mais um parâmetro em lista de expressão declarado"); }
    ;

%%

private Lexer lexer;
private Programa programa;
private boolean debugInterno = false;

private List<ParametroFuncao> parametrosAtuais = new LinkedList<>();

private void debugar(Object objeto) {
    if (debugInterno) {
        System.out.println(objeto);
    }
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Variavel> resto) {
    List<Variavel> variaveis = new LinkedList<Variavel>();
    variaveis.add(variavel);
    variaveis.addAll(resto);
    for (Variavel v : resto) {
        v.setTipo(variavel.getTipo());
    }

    DeclaracaoDeVariavel declaracao = new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);

    return declaracao;
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
    if ("syntax error".equals(mensagemErro)) {
        mensagemErro = "Erro de sintaxe";
    }
    throw new RuntimeException(montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.getLexema());
}

private void reportaErroSemantico(String mensagemErro, Token t) {
    programa.reportaErroSemantico(montaMensagemErro(mensagemErro, t));
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
        debugar(proximoToken);
        if (proximoToken.getTipo() != TipoToken.EOF) {
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

