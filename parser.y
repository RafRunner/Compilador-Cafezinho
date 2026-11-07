%language "Java"
%define api.package {src.raiz.generated}
%define api.parser.class {Parser}
%define api.parser.public
// Os valores ficam na pilha como Object, e o cast para os tipos genéricos (List<Variavel>...) não é verificável
%define api.parser.annotations {@SuppressWarnings("unchecked")}
%define api.value.type {Object}
%define parse.error custom
%define parse.lac full
%define parse.trace

%code imports {
    import java.io.*;
    import java.util.*;
    import java.util.stream.Stream;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.ast.declaracoes.*;
    import src.raiz.util.*;
}

// Terminais, com os mesmos nomes do enum TipoToken. O número de cada um sai daqui para o TabelasGramatica
%token <Token> PROGRAMA CAR INT FLUT VAZIO RETORNE LEIA ESCREVA NOVALINHA SE ENTAO SENAO
%token <Token> ENQUANTO EXECUTE PARA CONTINUE PARE COMO
%token <Token> OU E IGUAL DIFERENTE MENOR MAIOR MENOR_IGUAL MAIOR_IGUAL NEGACAO TERNARIO
%token <Token> MAIS MENOS VEZES DIVISAO RESTO
%token <Token> ATRIBUICAO VIRGULA PONTO_E_VIRGULA DOIS_PONTOS ABRE_CHAVE FECHA_CHAVE ABRE_PARENTESES
%token <Token> FECHA_PARENTESES ABRE_COLCHETE FECHA_COLCHETE
%token <Token> STRING_LITERAL CARACTERE_LITERAL IDENTIFICADOR INT_LITERAL FLUT_LITERAL

// Não terminais. O tipo é o do valor que a regra monta no $$; com ele o Bison já entrega cada $n convertido
%type <DeclaracaoFuncoesEVariaveis> DeclFuncVar
%type <BlocoPrograma> DeclProg
%type <List<Declarador>> DeclVar
%type <List<ParametroFuncao>> ListaParametrosCont
%type <ParametrosECorpo> DeclFunc
%type <List<ParametroFuncao>> ListaParametros
%type <BlocoDeclaracoes> Bloco
%type <DeclaracaoVariavelEmBloco> ListaDeclVar
%type <TipoVariavelNo> Tipo
%type <List<Comando>> ListaComando
%type <Comando> Comando
%type <ExpressaoIdentificador> LValueExpr
%type <Expressao> Expr AssignExpr CondExpr OrExpr AndExpr EqExpr DesigExpr AddExpr MulExpr ConvExpr UnExpr PrimExpr
%type <List<Expressao>> ListExpr

// Em diversas regras, criamos uma lista de valores e adicionamos no início dessa lista,
// isso é feito porque o analisador muitas vezes reduz regras correspondentes a partes
// posteriores do código primeiro. Se adicionássemos no final, a ordem ficaria invertida

// Em cada regra, é possível obter os valores dos não terminais (sempre Nós Sintáticos)
// ou terminais (sempre Tokens) usando $<posição na regra>. Também atribuímos um valor a
// uma regra (não terminal) com $$ = <Nó gerado pela regra>

// Definindo precedência na regra de derivação do se entao senao, para que um senao sempre se
// refira ao se mais próximo, sem ambiguidade ou decisão implícita
%nonassoc ENTAO
%nonassoc SENAO

%code {
private Programa programa = null;

// O que vem depois do nome numa declaração de função. O tipo e o nome ficam na regra de fora, que monta a DeclaracaoFuncao
private record ParametrosECorpo(List<ParametroFuncao> parametros, BlocoDeclaracoes corpo) {}

private record Declarador(Token nome, Integer tamanhoVetor) {}

public Programa getPrograma() {
    return programa;
}

public static Set<TipoToken> tokensEsperados(Context ctx) {
    SymbolKind[] esperados = new SymbolKind[Context.NTOKENS];
    int quantidade = ctx.getExpectedTokens(esperados, esperados.length);
    Set<TipoToken> tiposEsperados = EnumSet.noneOf(TipoToken.class);
    for (int i = 0; i < quantidade; i++) {
        SymbolKind simbolo = esperados[i];
        if (simbolo == SymbolKind.S_YYEOF) {
            tiposEsperados.add(TipoToken.EOF);
        } else {
            tiposEsperados.add(TipoToken.valueOf(simbolo.getName()));
        }
    }
    return tiposEsperados;
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Declarador> resto) {
    List<Variavel> variaveis = Stream.concat(
        Stream.of(variavel),
        resto.stream()
            .map(d -> new Variavel(variavel.getTipo(), d.nome(), d.tamanhoVetor()))
    ).toList();

    return new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.lexema());
}

private ComandoBloco blocoSemVariaveis(Token token, Comando... comandos) {
    return new ComandoBloco(token, new BlocoDeclaracoes(token, new DeclaracaoVariavelEmBloco(), List.of(comandos)));
}
}

%%

Programa:
    DeclFuncVar DeclProg {
        // Última regra a ser derivada. Termina de montar o programa.
        programa = new Programa($1, $2);
    }
  ;

DeclFuncVar:
      Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = $1;
        Token identificador = $2;

        List<Declarador> resto = $3;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = $5;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = $1;
        Token identificador = $2;
        Integer tamanhoVetor = tokenParaInt($4);

        List<Declarador> resto = $6;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = $8;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR DeclFunc DeclFuncVar {
        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao($2, $1, $3.corpo(), $3.parametros());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = $4;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().addFirst(declaracaoFuncao);

        $$ = outrasDeclaracoes;
    }
    | { $$ = new DeclaracaoFuncoesEVariaveis(); }
    ;

DeclProg:
    PROGRAMA Bloco {
        $$ = new BlocoPrograma($1, $2);
    }
    ;

DeclVar:
      VIRGULA IDENTIFICADOR DeclVar {
        Token identificador = $2;
        List<Declarador> variaveis = $3;

        Declarador declarador = new Declarador(identificador, null);
        variaveis.addFirst(declarador);

        $$ = variaveis;
    }
    | VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar {
        Token identificador = $2;
        Integer tamanhoVetor = tokenParaInt($4);
        List<Declarador> variaveis = $6;

        Declarador declarador = new Declarador($2, tamanhoVetor);
        variaveis.addFirst(declarador);

        $$ = variaveis;
    }
    | { $$ = new LinkedList<Declarador>(); }
    ;

DeclFunc:
     ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco {
        $$ = new ParametrosECorpo($2, $4);
     }
     ;

ListaParametros:
      /* vazio */ {
        $$ = new LinkedList<>();
    }
    | ListaParametrosCont {
    }
    ;

ListaParametrosCont:
      Tipo IDENTIFICADOR {
        List<ParametroFuncao> parametros = new LinkedList<>();
        parametros.add(new ParametroFuncao($2, $1, false));
        $$ = parametros;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE {
        List<ParametroFuncao> parametros = new LinkedList<>();
        parametros.add(new ParametroFuncao($2, $1, true));
        $$ = parametros;
    }
    | Tipo IDENTIFICADOR VIRGULA ListaParametrosCont {
        List<ParametroFuncao> parametros = $4;
        parametros.addFirst(new ParametroFuncao($2, $1, false));
        $$ = parametros;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont {
        List<ParametroFuncao> parametros = $6;
        parametros.addFirst(new ParametroFuncao($2, $1, true));
        $$ = parametros;
    }
    ;

Bloco:
      ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE {
        $$ = new BlocoDeclaracoes($1, $2, $3);
    }
    | ABRE_CHAVE ListaDeclVar FECHA_CHAVE {
        $$ = new BlocoDeclaracoes($1, $2, List.of());
    }
    ;

ListaDeclVar:
      /* vazio */ {
        $$ = new DeclaracaoVariavelEmBloco();
      }
    | Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar {
        Variavel variavel = new Variavel($1, $2, null);

        List<Declarador> outrasVariaveis = $3;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = $5;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar {
        Token tokenVariavel = $2;
        Variavel variavel = new Variavel($1, tokenVariavel, tokenParaInt($4));

        List<Declarador> outrasVariaveis = $6;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = $8;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    ;

Tipo:
      INT { $$ = new TipoVariavelNo($1, TipoVariavel.INTEIRO); }
    | FLUT { $$ = new TipoVariavelNo($1, TipoVariavel.FLUTUANTE); }
    | CAR { $$ = new TipoVariavelNo($1, TipoVariavel.CARACTERE); }
    | VAZIO { $$ = new TipoVariavelNo($1, TipoVariavel.VAZIO); }
    ;

ListaComando:
      Comando {
        List<Comando> comandos = new LinkedList<>();
        comandos.addFirst($1);
        $$ = comandos;
    }
    | Comando ListaComando  {
        List<Comando> comandos = $2;
        comandos.addFirst($1);
        $$ = comandos;
    }
    ;

Comando:
      PONTO_E_VIRGULA {
        $$ = new ComandoVazio($1);
    }
    | Expr PONTO_E_VIRGULA {
        Expressao expressao = $1;
        $$ = new ComandoComExpressao(expressao);
    }
    | RETORNE PONTO_E_VIRGULA {
        $$ = new ComandoRetorno($1, new ExpressaoVazio());
    }
    | RETORNE Expr PONTO_E_VIRGULA {
        Expressao expressao = $2;
        $$ = new ComandoRetorno($1, expressao);
    }
    | LEIA LValueExpr PONTO_E_VIRGULA {
        $$ = new ComandoLeia($1, $2);
    }
    | ESCREVA Expr PONTO_E_VIRGULA {
        $$ = new ComandoEscreva($1, $2);
    }
    | NOVALINHA PONTO_E_VIRGULA {
        $$ = new ComandoNovalinha($1);
    }
    | CONTINUE PONTO_E_VIRGULA {
        $$ = new ComandoContinue($1);
    }
    | PARE PONTO_E_VIRGULA {
        $$ = new ComandoPare($1);
    }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando %prec ENTAO {
        $$ = new ComandoSe($1, $3, $6);
    }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando {
        $$ = new ComandoSe($1, $3, $6, $8);
    }
    | ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando {
        $$ = new ComandoEnquanto($1, $3, $6);
    }
    | Bloco {
        BlocoDeclaracoes bloco = $1;
        $$ = new ComandoBloco(bloco.getToken(), bloco);
    }
    | PARA ABRE_PARENTESES Expr PONTO_E_VIRGULA Expr PONTO_E_VIRGULA Expr FECHA_PARENTESES EXECUTE Comando {
        Token para = $1;
        Expressao expressaoInicializacao = $3;
        Expressao expressaoCondicao = $5;
        ComandoComExpressao passo = new ComandoComExpressao($7);
        Comando corpo = $10;

        ComandoEnquanto enquanto = new ComandoEnquanto(para, expressaoCondicao, corpo, passo);

        ComandoBloco blocoFor = blocoSemVariaveis(
            para,
            new ComandoComExpressao(expressaoInicializacao),
            enquanto
        );

        $$ = blocoFor;
    }
    ;

Expr:
    AssignExpr
    ;

AssignExpr:
      CondExpr
    | LValueExpr ATRIBUICAO AssignExpr {
        ExpressaoIdentificador identificador = $1;
        $$ = new ExpressaoAtribuicao($2, identificador, $3);
    }
    ;

CondExpr:
      OrExpr
    | OrExpr TERNARIO Expr DOIS_PONTOS CondExpr {
        $$ = new ExpressaoTernaria($2, $1, $3, $5);
    }
    ;

OrExpr:
      OrExpr OU AndExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.OU, $1, $3);
    }
    | AndExpr
    ;

AndExpr:
      AndExpr E EqExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.E, $1, $3);
    }
    | EqExpr
    ;

EqExpr:
      EqExpr IGUAL DesigExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.IGUAL, $1, $3);
    }
    | EqExpr DIFERENTE DesigExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.DIFERENTE, $1, $3);
    }
    | DesigExpr
    ;

DesigExpr:
      DesigExpr MENOR AddExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.MENOR, $1, $3);
    }
    | DesigExpr MAIOR AddExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.MAIOR, $1, $3);
    }
    | DesigExpr MAIOR_IGUAL AddExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.MAIOR_IGUAL, $1, $3);
    }
    | DesigExpr MENOR_IGUAL AddExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.MENOR_IGUAL, $1, $3);
    }
    | AddExpr
    ;

AddExpr:
      AddExpr MAIS MulExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.SOMA, $1, $3);
    }
    | AddExpr MENOS MulExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.SUBTRACAO, $1, $3);
    }
    | MulExpr
    ;

MulExpr:
      MulExpr VEZES ConvExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.MULTIPLICACAO, $1, $3);
    }
    | MulExpr DIVISAO ConvExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.DIVISAO, $1, $3);
    }
    | MulExpr RESTO ConvExpr {
        $$ = new ExpressaoBinaria($2, OperadorBinario.RESTO, $1, $3);
    }
    | ConvExpr
    ;

ConvExpr:
      ConvExpr COMO Tipo {
        $$ = new ExpressaoConversao($2, $1, $3.getTipo());
    }
    | UnExpr
    ;

UnExpr:
      MENOS PrimExpr {
        $$ = new ExpressaoUnaria($1, OperadorUnario.NEGATIVO, $2);
    }
    | NEGACAO PrimExpr {
        $$ = new ExpressaoUnaria($1, OperadorUnario.NEGACAO, $2);
    }
    | PrimExpr
    ;

LValueExpr:
      IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE {
        $$ = new ExpressaoIdentificador($1, $3);
    }
    | IDENTIFICADOR {
        $$ = new ExpressaoIdentificador($1, null);
    }
    ;

PrimExpr:
      IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES {
        $$ = new ExpressaoChamadaFuncao($1, $3);
    }
    | IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES {
        $$ = new ExpressaoChamadaFuncao($1, new LinkedList<>());
    }
    | IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE {
        $$ = new ExpressaoIdentificador($1, $3);
    }
    | IDENTIFICADOR {
        $$ = new ExpressaoIdentificador($1, null);
    }
    | STRING_LITERAL {
        $$ = new ExpressaoStringLiteral($1);
    }
    | CARACTERE_LITERAL {
        $$ = new ExpressaoCaractereLiteral($1);
    }
    | INT_LITERAL {
        $$ = new ExpressaoInteiroLiteral($1);
    }
    | FLUT_LITERAL {
        $$ = new ExpressaoFlutuanteLiteral($1);
    }
    | ABRE_PARENTESES Expr FECHA_PARENTESES {
        Expressao expressao = $2;
        expressao.setEmParenteses(true);
        $$ = expressao;
    }
    ;

ListExpr:
    AssignExpr {
        List<Expressao> argumentos = new LinkedList<>();
        argumentos.add($1);
        $$ = argumentos;
    }
    | ListExpr VIRGULA AssignExpr {
        List<Expressao> argumentos = $1;
        argumentos.add($3);
        $$ = argumentos;
    }
    ;

%%
