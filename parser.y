%{
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.ast.declaracoes.*;
    import src.raiz.util.*;
%}

// Foi decidido que toda regra irá retornar um objeto, por sua flexibilidade

// Terminais. Extremamente importante que tenha a mesma ordem do enum TipoToken
%token <obj> PROGRAMA CAR INT FLUT RETORNE LEIA ESCREVA NOVALINHA SE ENTAO SENAO ENQUANTO EXECUTE
%token <obj> OU E IGUAL DIFERENTE MENOR MAIOR MENOR_IGUAL MAIOR_IGUAL NEGACAO TERNARIO
%token <obj> MAIS MENOS VEZES DIVISAO RESTO
%token <obj> ATRIBUICAO VIRGULA PONTO_E_VIRGULA DOIS_PONTOS ABRE_CHAVE FECHA_CHAVE ABRE_PARENTESES
%token <obj> FECHA_PARENTESES ABRE_COLCHETE FECHA_COLCHETE
%token <obj> STRING_LITERAL CARACTERE_LITERAL IDENTIFICADOR INT_LITERAL FLUT_LITERAL

// Não terminais
%type <obj> DeclFuncVar DeclVar Tipo Bloco ListaDeclVar, DeclProg, DeclFunc, ListaParametros
%type <obj> ListaComando, Comando, LValueExpr, Expr, AssignExpr, OrExpr, AndExpr, EqExpr
%type <obj> EqExpr, DesigExpr, AddExpr, MulExpr, UnExpr, PrimExpr, ListExpr, CondExpr

// Em diversas regras, criamos uma lista de valores e adicionamos no início dessa lista,
// isso é feito porque o analisador muitas vezes reduz regras correspondentes a partes
// posteriores do código primeiro. Se adicionássemos no final, a ordem ficaria invertida

// Em cada regra, é possível obter os valores dos não terminais (sempre Nós Sintáticos)
// ou terminais (sempre Tokens) usando $<posição na regra>. Também atribuímos um valor a
// uma regra (não terminal) com $$ = <Nó gerado pela regra>

%%

Programa:
    DeclFuncVar DeclProg {
        // Última regra a ser derivada. Termina de montar o programa.
        debugar("Programa derivado com sucesso");
        this.programa.setDeclaracaoFuncoesEVariaveis((DeclaracaoFuncoesEVariaveis) $1);
        this.programa.setBlocoPrograma((BlocoPrograma) $2);
    }
  ;

DeclFuncVar:
      Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável " + identificador.lexema());

        List<Variavel> resto = (List<Variavel>) $3;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $5;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar {
        TipoVariavelNo tipo = (TipoVariavelNo) $1;
        Token identificador = (Token) $2;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) $4, (Token) $2);

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável vetor "
            + identificador.lexema() + " tamanho: " + tamanhoVetor);

        List<Variavel> resto = (List<Variavel>) $6;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $8;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        $$ = outrasDeclaracoes;
    }
    | Tipo IDENTIFICADOR DeclFunc DeclFuncVar {
        List<ParametroFuncao> parametros = (List<ParametroFuncao>) ((Object[]) $3)[0];
        BlocoDeclaracoes corpo = (BlocoDeclaracoes) ((Object[]) $3)[1];

        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao((Token) $2, (TipoVariavelNo) $1, corpo, parametros);
        debugar("Declaracao de função " + declaracaoFuncao.getNome() + " " + declaracaoFuncao.getTipoRetorno());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) $4;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().addFirst(declaracaoFuncao);

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

        debugar("Declaração de variável " + identificador.lexema());

        Variavel variavel = new Variavel(null, identificador, null);
        variaveis.addFirst(variavel);

        $$ = variaveis;
    }
    | VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar {
        Token identificador = (Token) $2;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) $4, identificador);
        List<Variavel> variaveis = (List<Variavel>) $6;

        debugar("Declaração de variável vetor " + identificador.lexema() + " tamanho: " + tamanhoVetor);

        Variavel variavel = new Variavel(null, (Token) $2, tamanhoVetor);
        variaveis.addFirst(variavel);

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
        parametrosAtuais.addFirst(new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, false));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE {
        debugar("Útimo parâmetro vetor " + getLexema($2) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, true));
    }
    | Tipo IDENTIFICADOR VIRGULA ListaParametrosCont {
        debugar("Parâmetro " + getLexema($2) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, false));
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont {
        debugar("Parâmetro vetor " + getLexema($2) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) $2, (TipoVariavelNo) $1, true));
    }
    ;

Bloco:
      ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE {
        debugar("Bloco com comandos derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) $1);
        bloco.getDeclaracoes().add((Declaracao) $2);
        bloco.getDeclaracoes().addAll((List<Declaracao>) $3);

        $$ = bloco;
    }
    | ABRE_CHAVE ListaDeclVar FECHA_CHAVE { 
        debugar("Bloco somente com variáveis derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) $1);
        bloco.getDeclaracoes().add((Declaracao) $2);

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
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    | Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar {
        Token tokenVariavel = (Token) $2;
        Variavel variavel = new Variavel((TipoVariavelNo) $1, tokenVariavel, tokenParaTamanhoVetor((Token) $4, tokenVariavel));

        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável vetor "
            + variavel.getNome() + " tamanho: " + variavel.getTamanhoVetor());

        List<Variavel> outrasVariaveis = (List<Variavel>) $6;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) $8;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        $$ = declaracaoVariavelEmBloco;
    }
    ;

Tipo:
      INT { debugar("Tipo: " + getLexema($1)); $$ = new TipoVariavelNo((Token) $1, TipoVariavel.INTEIRO); }
    | FLUT { debugar("Tipo: " + getLexema($1)); $$ = new TipoVariavelNo((Token) $1, TipoVariavel.FLUTUANTE); }
    | CAR { debugar("Tipo: " + getLexema($1)); $$ = new TipoVariavelNo((Token) $1, TipoVariavel.CARACTERE); }
    ;

ListaComando:
      Comando {
        debugar("Primeiro comando derivado");
        List<Comando> comandos = new LinkedList<>();
        comandos.addFirst((Comando) $1);
        $$ = comandos;
    }
    | Comando ListaComando  {
        debugar("Comando derivado");
        List<Comando> comandos = (List<Comando>) $2;
        comandos.addFirst((Comando) $1);
        $$ = comandos;
    }
    ;

Comando:
      PONTO_E_VIRGULA {
        debugar("Comando vazio ';'");
        // Comando vazio
        $$ = null;
    }
    | Expr PONTO_E_VIRGULA {
        debugar("Comando com expressão");
        Expressao expressao = (Expressao) $1;
        $$ = new ComandoComExpressao(expressao.getToken(), expressao);
    }
    | RETORNE Expr PONTO_E_VIRGULA {
        debugar("Comando de retorno");
        Expressao expressao = (Expressao) $2;
        $$ = new ComandoRetorno((Token) $1, expressao);
    }
    | LEIA LValueExpr PONTO_E_VIRGULA {
        debugar("Comando leia");
        $$ = new ComandoLeia((Token) $1, (ExpressaoIdentificador) $2);
    }
    | ESCREVA Expr PONTO_E_VIRGULA {
        debugar("Comando escreva");
        $$ = new ComandoEscreva((Token) $1, (Expressao) $2);
    }
    | ESCREVA STRING_LITERAL PONTO_E_VIRGULA {
        debugar("Comando escreva String " + getLexema($2));
        ExpressaoStringLiteral expressao = new ExpressaoStringLiteral((Token) $2);

        $$ = new ComandoEscreva((Token) $1, expressao);
    }
    | NOVALINHA PONTO_E_VIRGULA {
        debugar("Comando novalinha");
        $$ = new ComandoNovalinha((Token) $1);
    }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando {
        debugar("Comando se simples");
        $$ = new ComandoSe((Token) $1, (Expressao) $3, (Comando) $6);
    }
    | SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando {
        debugar("Comando se senao");
        $$ = new ComandoSe((Token) $1, (Expressao) $3, (Comando) $6, (Comando) $8);
    }
    | ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando {
        debugar("Comando enquanto");
        $$ = new ComandoEnquanto((Token) $1, (Expressao) $3, (Comando) $6);
    }
    | Bloco {
        debugar("Comando bloco");
        BlocoDeclaracoes bloco = (BlocoDeclaracoes) $1;
        $$ = new ComandoBloco(bloco.getToken(), bloco);
    }
    ;

Expr:
    AssignExpr {
        debugar("Expr derivada");
        $$ = (Expressao) $1;
    }
    ;

AssignExpr:
      CondExpr {
        debugar("Expressão CondExpr derivada");
        $$ = (Expressao) $1;
    }
    | LValueExpr ATRIBUICAO AssignExpr {
        debugar("Expressão de atribuição derivada");
        ExpressaoIdentificador identificador = (ExpressaoIdentificador) $1;
        $$ = new ExpressaoAtribuicao((Token) $2, identificador, (Expressao) $3);
    }
    ;

CondExpr:
      OrExpr { debugar("Expressão OrExpr derivada"); }
    | OrExpr TERNARIO Expr DOIS_PONTOS CondExpr {
        debugar("Expressão ou ternária derivada");
        $$ = new ExpressaoTernaria((Token) $2, (Expressao) $1, (Expressao) $3, (Expressao) $5);
    }
    ;

OrExpr:
      OrExpr OU AndExpr {
        debugar("Expressão ou derivada");
        $$ = new ExpressaoOu((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | AndExpr {
        debugar("Expressão e derivada");
        $$ = (Expressao) $1;
    }
    ;

AndExpr:
      AndExpr E EqExpr {
        debugar("Expressão e derivada");
        $$ = new ExpressaoE((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | EqExpr {
        debugar("Expressão EqExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

EqExpr:
      EqExpr IGUAL DesigExpr {
        debugar("Expressão igual derivada");
        $$ = new ExpressaoIgual((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | EqExpr DIFERENTE DesigExpr {
        debugar("Expressão diferente derivada");
        $$ = new ExpressaoDiferente((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | DesigExpr {
        debugar("Expressão DesigExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

DesigExpr:
      DesigExpr MENOR AddExpr {
        debugar("Expressão menor derivada");
        $$ = new ExpressaoMenor((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | DesigExpr MAIOR AddExpr {
        debugar("Expressão maior derivada");
        $$ = new ExpressaoMaior((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | DesigExpr MAIOR_IGUAL AddExpr {
        debugar("Expressão maior igual derivada");
        $$ = new ExpressaoMaiorIgual((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | DesigExpr MENOR_IGUAL AddExpr {
        debugar("Expressão menor igual derivada");
        $$ = new ExpressaoMenorIgual((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | AddExpr {
        debugar("Expressão AddExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

AddExpr:
      AddExpr MAIS MulExpr {
        debugar("Expressão mais derivada");
        $$ = new ExpressaoMais((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | AddExpr MENOS MulExpr {
        debugar("Expressão menos derivada");
        $$ = new ExpressaoMenos((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | MulExpr {
        debugar("Expressão MulExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

MulExpr:
      MulExpr VEZES UnExpr {
        debugar("Expressão vezes derivada");
        $$ = new ExpressaoVezes((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | MulExpr DIVISAO UnExpr {
        debugar("Expressão divisão derivada");
        $$ = new ExpressaoDivisao((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | MulExpr RESTO UnExpr {
        debugar("Expressão resto derivada");
        $$ = new ExpressaoResto((Token) $2, (Expressao) $1, (Expressao) $3);
    }
    | UnExpr {
        debugar("Expressão UnExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

UnExpr:
      MENOS PrimExpr {
        debugar("Expressão menos unária derivada");
        $$ = new ExpressaoNegativo((Token) $1, (Expressao) $2);
    }
    | NEGACAO PrimExpr {
        debugar("Expressão negação derivada");
        $$ = new ExpressaoNegacao((Token) $1, (Expressao) $2);
    }
    | PrimExpr {
        debugar("Expressão PrimExpr derivada");
        $$ = (Expressao) $1;
    }
    ;

LValueExpr:
      IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE {
        debugar("Expressão de indexação no vetor " + getLexema($1));
        $$ = new ExpressaoIdentificador((Token) $1, (Expressao) $3);
    }
    | IDENTIFICADOR {
        debugar("Expressão de identificador(LValueExpr) " + getLexema($1));
        $$ = new ExpressaoIdentificador((Token) $1, null);
    }
    ;

PrimExpr:
      IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES {
        debugar("Expressão de chamada da função " + getLexema($1));
        $$ = new ExpressaoChamadaFuncao((Token) $1, (LinkedList<Expressao>) $3);
    }
    | IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES {
        debugar("Expressão de chamada da função sem argumentos " + getLexema($1));
        $$ = new ExpressaoChamadaFuncao((Token) $1, new LinkedList<>());
    }
    | IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE {
        debugar("Expressão de indexação no vetor " + getLexema($1));
        $$ = new ExpressaoIdentificador((Token) $1, (Expressao) $3);
    }
    | IDENTIFICADOR {
        debugar("Expressão de identificador(PrimExpr) " + getLexema($1));
        $$ = new ExpressaoIdentificador((Token) $1, null);
    }
    | STRING_LITERAL {
        debugar("Literal string " + getLexema($1));
        $$ = new ExpressaoStringLiteral((Token) $1);
    }
    | CARACTERE_LITERAL {
        debugar("Literal caractere " + getLexema($1));
        $$ = new ExpressaoCaractereLiteral((Token) $1);
    }
    | INT_LITERAL {
        debugar("Literal inteiro " + getLexema($1));
        $$ = new ExpressaoInteiroLiteral((Token) $1);
    }
    | FLUT_LITERAL {
        debugar("Literal flutuante " + getLexema($1));
        $$ = new ExpressaoFlutuanteLiteral((Token) $1);
    }
    | ABRE_PARENTESES Expr FECHA_PARENTESES {
        debugar("Expressão entre parênteses derivada");
        $$ = new ExpressaoEntreParenteses((Token) $1, (Expressao) $2);
    }
    ;

ListExpr:
    AssignExpr {
        debugar("Lista de expressões iniciada.");
        LinkedList<Expressao> parametrosChamadaAtuais = new LinkedList<>();
        parametrosChamadaAtuais.add((Expressao) $1);
        $$ = parametrosChamadaAtuais;
    }
    | ListExpr VIRGULA AssignExpr {
        debugar("Mais um parâmetro em lista de expressão declarado");
        LinkedList<Expressao> parametrosChamadaAtuais = (LinkedList<Expressao>) $1;
        parametrosChamadaAtuais.add((Expressao) $3);
    }
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
    List<Variavel> variaveis = new LinkedList<>();
    variaveis.add(variavel);
    variaveis.addAll(resto);

    for (Variavel v : resto) {
        v.setTipo(variavel.getTipo());
    }

    DeclaracaoDeVariavel declaracao = new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);

    return declaracao;
}

private String getLexema(Object token) {
    return ((Token) token).lexema();
}

private void yyerror(String mensagemErro) {
    if ("syntax error".equals(mensagemErro)) {
        mensagemErro = "Erro de sintaxe";
    }
    throw new RuntimeException(AstUtil.montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.lexema());
}

// Transforma um token em um número. Como vetores tem que ter tamanho 1 no mínimo, 0 aqui é um erro semântico
private Integer tokenParaTamanhoVetor(Token numero, Token variavel) {
    Integer tamanho = tokenParaInt(numero);
    if (tamanho == 0) {
        this.programa.reportaErroSemantico("Array " + variavel.lexema() + " não pode ter tamanho 0", variavel);
    }

    return tamanho;
}

// Função que traduz o Token do lexer para um valor int do BYACC/J
private int yylex() {
    try {
        Token proximoToken = lexer.yylex();
        debugar(proximoToken);
        if (proximoToken.tipo() != TipoToken.EOF) {
            yylval = new ParserVal(proximoToken);

            // Os valores de 0 a 256 são reservados, por isso adicionamos 257
            return proximoToken.tipo().ordinal() + 257;
        } else {
            return 0; // EOF
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

public Programa analisar(Reader leitor, boolean debugInterno) {
    programa = new Programa(); // Cria um novo objeto de programa
    lexer = new Lexer(leitor); // Cria Lexer para ler o arquivo
    this.debugInterno = debugInterno;
    yyparse(); // Cria a AST fazendo análise sintática e semântica

    // Se existem erros semânticos, reportamos e abortamos a geração do programa
    if (!programa.getErrosSemanticos().isEmpty()) {
        StringBuilder sb = new StringBuilder("Erros semânticos foram identificados na análise:\n");

        for (String mensagem : programa.getErrosSemanticos()) {
            sb.append(mensagem).append("\n");
        }

        throw new RuntimeException(sb.toString());
    }

    return programa;
}

