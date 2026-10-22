package src.raiz.semantica;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.Programa;
import src.raiz.ast.TipoVariavel;
import src.raiz.ast.TipoVariavelNo;
import src.raiz.ast.Variavel;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.FuncoesNativas;
import src.raiz.compilador.ParametroFuncaoNativa;
import src.raiz.erros.BugCompilador;
import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;

import java.util.List;
import java.util.function.Supplier;

// Valida as regras da linguagem e anota a AST para a geração de código: toda expressão recebe seu tipo,
// todo identificador o símbolo da variável a que se refere e toda chamada a função chamada.
// Não sabe nada sobre a arquitetura alvo: depois dele, um backend só precisa ler as anotações
public class AnalisadorSemantico {

    private final TabelaDeSimbolos tabelaGlobal = new TabelaDeSimbolos();

    // Função sendo analisada. Null quando estamos no bloco programa
    private DeclaracaoFuncao funcaoAtual = null;

    public void analisar(Programa programa) {
        verificarEstrutura(programa);

        analisarDeclaracoesGlobais(programa.getDeclaracaoFuncoesEVariaveis());

        funcaoAtual = null;
        analisarBloco(programa.getBlocoPrograma().getBlocoDeclaracoes(), tabelaGlobal.criaBlocoInterno());
    }

    // Primeiro passe: regras que dependem só da forma do programa, não de nomes ou tipos. Os blocos são
    // verificados de dentro para fora, para que o erro reportado seja o mais específico
    private void verificarEstrutura(Programa programa) {
        for (Declaracao declaracao : programa.getDeclaracaoFuncoesEVariaveis().getDeclaracoesEmOrdem()) {
            switch (declaracao) {
                case DeclaracaoDeVariavel declaracaoDeVariavel -> declaracaoDeVariavel.getVariaveis().forEach(this::validaTipoNaoVazio);
                case DeclaracaoFuncao funcao -> verificarEstruturaFuncao(funcao);
                default -> throw new BugCompilador("Declaração global não esperada " + declaracao.getClass());
            }
        }

        verificarEstruturaBloco(programa.getBlocoPrograma().getBlocoDeclaracoes());
    }

    private void verificarEstruturaFuncao(DeclaracaoFuncao funcao) {
        for (ParametroFuncao parametro : funcao.getParametros()) {
            if (parametro.getTipo().isTipoVazio()) {
                throw new ErroSemantico(
                        "Parâmetro '" + parametro.getNome() + "' não pode ter tipo vazio",
                        parametro.getToken()
                );
            }
        }

        verificarEstruturaBloco(funcao.getCorpo());

        if (!funcao.getTipoRetorno().isTipoVazio() && !terminaComRetorno(funcao.getCorpo())) {
            throw new ErroSemantico(
                    "Função '" + funcao.getNome() + "' deve terminar com expressão de retorno",
                    funcao.getToken()
            );
        }
    }

    private void verificarEstruturaBloco(BlocoDeclaracoes bloco) {
        for (Declaracao declaracao : bloco.getDeclaracoes()) {
            switch (declaracao) {
                case DeclaracaoVariavelEmBloco declaracaoVariaveis -> declaracaoVariaveis.getDeclaracoesDeVariaveis()
                        .forEach(declaracaoDeVariavel -> declaracaoDeVariavel.getVariaveis().forEach(this::validaTipoNaoVazio));
                case Comando comando -> verificarEstruturaComando(comando);
                default -> throw new BugCompilador("Declaração não esperada em bloco " + declaracao.getClass());
            }
        }

        List<Declaracao> declaracoes = bloco.getDeclaracoes();
        for (int i = 0; i < declaracoes.size() - 1; i++) {
            if (declaracoes.get(i) instanceof ComandoRetorno) {
                throw new ErroSemantico("Código inacessível. Comando retorno deve ser o último do bloco", bloco.getToken());
            }
        }
    }

    // Procura blocos aninhados nos comandos
    private void verificarEstruturaComando(Comando comando) {
        switch (comando) {
            case ComandoBloco comandoBloco -> verificarEstruturaBloco(comandoBloco.getDeclaracoes());
            case ComandoSe comandoSe -> {
                verificarEstruturaComando(comandoSe.getConsequencia());
                if (comandoSe.getAlternativa() != null) {
                    verificarEstruturaComando(comandoSe.getAlternativa());
                }
            }
            case ComandoEnquanto comandoEnquanto -> verificarEstruturaComando(comandoEnquanto.getComando());
            case ComandoComExpressao ignored -> {
            }
            case ComandoEscreva ignored -> {
            }
            case ComandoLeia ignored -> {
            }
            case ComandoNovalinha ignored -> {
            }
            case ComandoRetorno ignored -> {
            }
        }
    }

    private void validaTipoNaoVazio(Variavel variavel) {
        if (variavel.getTipo().isTipoVazio()) {
            throw new ErroSemantico("Variável '" + variavel.getNome() + "' não pode ter tipo vazio", variavel.getToken());
        }
    }

    private boolean terminaComRetorno(BlocoDeclaracoes bloco) {
        if (bloco.getDeclaracoes().isEmpty()) {
            return false;
        }

        return terminaComRetorno(bloco.getDeclaracoes().getLast());
    }

    private boolean terminaComRetorno(Declaracao declaracao) {
        return switch (declaracao) {
            case ComandoRetorno ignored -> true;
            case ComandoBloco comandoBloco -> terminaComRetorno(comandoBloco.getDeclaracoes());
            // Um 'se' só garante o retorno se tiver 'senao' e os dois caminhos retornarem
            case ComandoSe comandoSe -> comandoSe.getAlternativa() != null
                                        && terminaComRetorno(comandoSe.getConsequencia())
                                        && terminaComRetorno(comandoSe.getAlternativa());
            default -> false;
        };
    }

    // Segundo passe: resolve nomes, verifica tipos e anota a AST.
    // Funções e variáveis globais são processadas na ordem em que aparecem: uma função só enxerga
    // as globais e funções declaradas antes dela (e ela mesma, permitindo recursão)
    private void analisarDeclaracoesGlobais(DeclaracaoFuncoesEVariaveis declaracoes) {
        for (Declaracao declaracao : declaracoes.getDeclaracoesEmOrdem()) {
            switch (declaracao) {
                case DeclaracaoDeVariavel declaracaoDeVariavel -> {
                    for (Variavel variavel : declaracaoDeVariavel.getVariaveis()) {
                        tabelaGlobal.adicionaSimbolo(new SimboloVariavelGlobal(variavel));
                    }
                }
                case DeclaracaoFuncao funcao -> analisarFuncao(funcao);
                default -> throw new BugCompilador("Declaração global não esperada " + declaracao.getClass());
            }
        }
    }

    private void analisarFuncao(DeclaracaoFuncao funcao) {
        // Os parâmetros ficam no mesmo escopo do corpo da função
        TabelaDeSimbolos tabelaFuncao = tabelaGlobal.criaBlocoInterno();
        tabelaGlobal.adicionaSimbolo(new SimboloFuncao(funcao));

        for (ParametroFuncao parametro : funcao.getParametros()) {
            tabelaFuncao.adicionaSimbolo(new SimboloParametro(parametro));
        }

        funcaoAtual = funcao;
        analisarBloco(funcao.getCorpo(), tabelaFuncao);
    }

    private void analisarBloco(BlocoDeclaracoes bloco, TabelaDeSimbolos tabela) {
        for (Declaracao declaracao : bloco.getDeclaracoes()) {
            switch (declaracao) {
                case DeclaracaoVariavelEmBloco declaracaoVariaveis -> {
                    for (DeclaracaoDeVariavel declaracaoDeVariavel : declaracaoVariaveis.getDeclaracoesDeVariaveis()) {
                        for (Variavel variavel : declaracaoDeVariavel.getVariaveis()) {
                            tabela.adicionaSimbolo(new SimboloVariavelLocal(variavel));
                        }
                    }
                }
                case Comando comando -> analisarComando(comando, tabela);
                default -> throw new BugCompilador("Declaração não esperada em bloco " + declaracao.getClass());
            }
        }
    }

    private void analisarComando(Comando comando, TabelaDeSimbolos tabela) {
        switch (comando) {
            case ComandoBloco comandoBloco -> analisarBloco(comandoBloco.getDeclaracoes(), tabela.criaBlocoInterno());
            case ComandoComExpressao comandoComExpressao -> analisarExpressao(comandoComExpressao.getExpressao(), tabela);
            case ComandoEscreva comandoEscreva -> {
                if (analisarExpressao(comandoEscreva.getExpressao(), tabela) == TipoVariavel.VAZIO) {
                    throw new ErroSemantico("Não se pode escrever valor vazio", comandoEscreva.getToken());
                }
            }
            case ComandoLeia comandoLeia -> analisarLeia(comandoLeia, tabela);
            case ComandoNovalinha ignored -> {
            }
            case ComandoRetorno comandoRetorno -> analisarRetorno(comandoRetorno, tabela);
            case ComandoSe comandoSe -> {
                analisarCondicional(comandoSe.getSe(), tabela);
                analisarComando(comandoSe.getConsequencia(), tabela);
                if (comandoSe.getAlternativa() != null) {
                    analisarComando(comandoSe.getAlternativa(), tabela);
                }
            }
            case ComandoEnquanto comandoEnquanto -> {
                analisarCondicional(comandoEnquanto.getCondicional(), tabela);
                analisarComando(comandoEnquanto.getComando(), tabela);
            }
        }
    }

    private void analisarLeia(ComandoLeia comandoLeia, TabelaDeSimbolos tabela) {
        ExpressaoIdentificador alvo = comandoLeia.getExpressaoIdentificador();
        Simbolo simbolo = getSimbolo(alvo.getToken(), tabela, alvo.getIdentificador());

        // Ler um valor é atribuí-lo à variável, então as mesmas regras de atribuição se aplicam
        analisarAtribuicao(alvo, simbolo, comandoLeia.getToken(), tabela, () -> {
            TipoVariavel tipo = ((SimboloVariavel) simbolo).getTipo();
            if (tipo != TipoVariavel.INTEIRO && tipo != TipoVariavel.CARACTERE) {
                throw new ErroSemantico("Tipo " + tipo + " não suportado para leitura", comandoLeia.getToken());
            }
            return tipo;
        });
    }

    private void analisarRetorno(ComandoRetorno comandoRetorno, TabelaDeSimbolos tabela) {
        TipoVariavel tipoRetorno = analisarExpressao(comandoRetorno.getExpressao(), tabela);

        // No bloco programa, o valor retornado vira o código de saída e qualquer tipo é aceito
        if (funcaoAtual == null) {
            return;
        }

        TipoVariavelNo tipoEsperado = funcaoAtual.getTipoRetorno();
        if (tipoRetorno != tipoEsperado.getTipo()) {
            String mensagem;
            if (tipoEsperado.isTipoVazio()) {
                mensagem = "Não se pode retornar valor de função com retorno vazio na função '" + funcaoAtual.getNome() + "'";
            } else {
                mensagem = "Tipo de retorno incompatível na função '" + funcaoAtual.getNome() + "' Esperado: "
                           + tipoEsperado + ", recebido: " + tipoRetorno;
            }
            throw new ErroSemantico(mensagem, comandoRetorno.getToken());
        }
    }

    // Analisa a expressão, anota seu tipo e o retorna
    private TipoVariavel analisarExpressao(Expressao expressao, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = switch (expressao) {
            case ExpressaoEntreParenteses entreParenteses -> analisarExpressao(entreParenteses.getExpressao(), tabela);
            case ExpressaoIdentificador identificador -> analisarIdentificador(identificador, tabela);
            case ExpressaoInteiroLiteral ignored -> TipoVariavel.INTEIRO;
            case ExpressaoFlutuanteLiteral ignored -> TipoVariavel.FLUTUANTE;
            case ExpressaoCaractereLiteral ignored -> TipoVariavel.CARACTERE;
            case ExpressaoStringLiteral ignored -> TipoVariavel.STRING;
            case ExpressaoAtribuicao atribuicao -> {
                ExpressaoIdentificador alvo = atribuicao.getIdentificador();
                Simbolo simbolo = getSimbolo(atribuicao.getToken(), tabela, alvo.getIdentificador());
                yield analisarAtribuicao(alvo, simbolo, atribuicao.getToken(), tabela,
                        () -> analisarExpressao(atribuicao.getExpressaoLadoDireito(), tabela));
            }
            case ExpressaoBinaria binaria -> analisarBinaria(binaria, tabela);
            case ExpressaoUnaria unaria -> analisarUnaria(unaria, tabela);
            case ExpressaoTernaria ternaria -> analisarTernaria(ternaria, tabela);
            case ExpressaoChamadaFuncao chamada -> analisarChamada(chamada, tabela);
            case ExpressaoVazio ignored -> TipoVariavel.VAZIO;
        };

        expressao.setTipo(tipo);
        return tipo;
    }

    // Para expressões cujo valor é usado: uma chamada de função vazio não produz valor
    private TipoVariavel analisarValor(Expressao expressao, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = analisarExpressao(expressao, tabela);
        if (tipo == TipoVariavel.VAZIO) {
            throw new ErroSemantico("Função de retorno vazio não pode ser usada aqui", expressao.getToken());
        }

        return tipo;
    }

    private void analisarCondicional(Expressao expressao, TabelaDeSimbolos tabela) {
        if (analisarExpressao(expressao, tabela) != TipoVariavel.INTEIRO) {
            throw new ErroSemantico("Condicional deve retornar tipo booleano (inteiro)", expressao.getToken());
        }
    }

    private TipoVariavel analisarIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        String nome = identificador.getIdentificador();
        Simbolo simbolo = getSimbolo(identificador.getToken(), tabela, nome);

        if (!(simbolo instanceof SimboloVariavel variavel)) {
            throw new ErroSemantico("'" + nome + "' é uma função, deve ser invocada", identificador.getToken());
        }
        if (!variavel.isVetor() && identificador.getIndex() != null) {
            throw new ErroSemantico("Variável '" + nome + "' não é um vetor, não pode ser indexada", identificador.getToken());
        }
        if (variavel.isVetor() && identificador.getIndex() == null) {
            throw new ErroSemantico(
                    "Vetor '" + nome + "' deve ser indexado, só pode ser usado sem índice como argumento de função",
                    identificador.getToken()
            );
        }

        if (variavel.isVetor()) {
            analisarIndex(identificador, tabela);
        }

        identificador.setSimbolo(variavel);
        return variavel.getTipo();
    }

    private void analisarIndex(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        if (analisarExpressao(identificador.getIndex(), tabela) != TipoVariavel.INTEIRO) {
            throw new ErroSemantico("Expressão de index deve ser um inteiro!", identificador.getToken());
        }
    }

    // Usada pela atribuição e pelo comando leia. analisarValor analisa o valor atribuído e retorna seu tipo
    private TipoVariavel analisarAtribuicao(
            ExpressaoIdentificador alvo,
            Simbolo simbolo,
            Token token,
            TabelaDeSimbolos tabela,
            Supplier<TipoVariavel> analisarValor
    ) {
        String nome = alvo.getIdentificador();

        if (!(simbolo instanceof SimboloVariavel variavel)) {
            throw new ErroSemantico("'" + nome + "' é uma função, não pode ter valor atribuído", token);
        }
        if (!variavel.isVetor() && alvo.getIndex() != null) {
            throw new ErroSemantico("Variável '" + nome + "' não é um vetor, não pode ser indexada", token);
        }
        if (variavel.isVetor() && alvo.getIndex() == null) {
            throw new ErroSemantico("Vetor '" + nome + "' só pode receber atribuição com índice", token);
        }

        TipoVariavel tipoValor = analisarValor.get();
        if (tipoValor != variavel.getTipo()) {
            String mensagem = "A variável '" + nome + "' é do tipo " + variavel.getTipo()
                              + " e não pode receber valor do tipo " + tipoValor;
            throw new ErroSemantico(mensagem, token);
        }

        if (variavel.isVetor()) {
            analisarIndex(alvo, tabela);
        }

        alvo.setSimbolo(variavel);
        alvo.setTipo(variavel.getTipo());
        return tipoValor;
    }

    private TipoVariavel analisarBinaria(ExpressaoBinaria expressao, TabelaDeSimbolos tabela) {
        OperadorBinario operador = expressao.getOperador();
        String descricao = operador.getDescricao();

        // Os operandos lógicos são validados um a um, pois o direito pode nem ser avaliado (curto-circuito)
        if (operador.getCategoria() == OperadorBinario.Categoria.LOGICA) {
            exigeInteiro(expressao, analisarValor(expressao.getEsquerda(), tabela));
            exigeInteiro(expressao, analisarValor(expressao.getDireita(), tabela));
            return TipoVariavel.INTEIRO;
        }

        TipoVariavel esquerdo = analisarValor(expressao.getEsquerda(), tabela);
        TipoVariavel direito = analisarValor(expressao.getDireita(), tabela);

        return switch (operador.getCategoria()) {
            case ARITMETICA -> {
                if (!esquerdo.isNumerico() || !direito.isNumerico()) {
                    throw new ErroSemantico(descricao + " só pode ser aplicado entre valores numéricos", expressao.getToken());
                }
                // Entre int e flut, o int é convertido e o resultado é flut
                yield esquerdo == TipoVariavel.FLUTUANTE || direito == TipoVariavel.FLUTUANTE
                        ? TipoVariavel.FLUTUANTE
                        : esquerdo;
            }
            case INTEIRA -> {
                exigeInteiro(expressao, esquerdo);
                exigeInteiro(expressao, direito);
                yield TipoVariavel.INTEIRO;
            }
            case COMPARACAO -> {
                if (esquerdo != direito) {
                    throw new ErroSemantico(
                            "Só se pode realizar operação " + descricao + " com valores do mesmo tipo!",
                            expressao.getDireita().getToken()
                    );
                }
                yield TipoVariavel.INTEIRO;
            }
            case LOGICA -> throw new BugCompilador("Operador lógico já tratado");
        };
    }

    private void exigeInteiro(ExpressaoBinaria expressao, TipoVariavel tipo) {
        if (tipo != TipoVariavel.INTEIRO) {
            throw new ErroSemantico(
                    expressao.getOperador().getDescricao() + " só pode ser aplicado entre inteiros",
                    expressao.getToken()
            );
        }
    }

    private TipoVariavel analisarUnaria(ExpressaoUnaria expressao, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = analisarValor(expressao.getExpressao(), tabela);

        switch (expressao.getOperador()) {
            case NEGACAO -> {
                if (tipo != TipoVariavel.INTEIRO) {
                    throw new ErroSemantico("Não se pode aplicar negação a valores não booleanos (inteiros)", expressao.getToken());
                }
            }
            case NEGATIVO -> {
                if (!tipo.isNumerico()) {
                    throw new ErroSemantico("Não se pode aplicar negativo a valores não numéricos", expressao.getToken());
                }
            }
        }

        return tipo;
    }

    private TipoVariavel analisarTernaria(ExpressaoTernaria expressao, TabelaDeSimbolos tabela) {
        analisarCondicional(expressao.getCondicao(), tabela);
        TipoVariavel tipoSe = analisarValor(expressao.getSe(), tabela);
        TipoVariavel tipoSenao = analisarValor(expressao.getSenao(), tabela);

        if (tipoSe != tipoSenao) {
            throw new ErroSemantico("Os ramos da expressão ternária devem retornar o mesmo tipo.", expressao.getToken());
        }

        return tipoSe;
    }

    private TipoVariavel analisarChamada(ExpressaoChamadaFuncao chamada, TabelaDeSimbolos tabela) {
        String nome = chamada.getNomeFuncao();
        Simbolo simbolo = tabela.getSimbolo(nome);

        if (simbolo == null) {
            throw new ErroSemantico("A função '" + nome + "' não foi declarada", chamada.getToken());
        }

        TipoVariavel tipoRetorno = switch (simbolo) {
            case SimboloFuncaoNativa nativa -> analisarChamadaNativa(chamada, nativa.funcao(), tabela);
            case SimboloFuncao funcao -> analisarChamadaFuncao(chamada, funcao.declaracao(), tabela);
            case SimboloVariavel ignored -> throw new ErroSemantico("'" + nome + "' não é uma função", chamada.getToken());
        };

        chamada.setFuncao((SimboloInvocavel) simbolo);
        return tipoRetorno;
    }

    private TipoVariavel analisarChamadaNativa(ExpressaoChamadaFuncao chamada, FuncoesNativas funcao, TabelaDeSimbolos tabela) {
        List<Expressao> argumentos = chamada.getArgumentos();
        if (funcao.parametros.size() != argumentos.size()) {
            String mensagem = String.format("Função nativa '%s' espera receber %d argumento(s), mas recebeu %d",
                    funcao.nome, funcao.parametros.size(), argumentos.size());
            throw new ErroSemantico(mensagem, chamada.getToken());
        }

        for (int i = 0; i < argumentos.size(); i++) {
            ParametroFuncaoNativa parametro = funcao.parametros.get(i);
            TipoVariavel tipoArgumento = analisarExpressao(argumentos.get(i), tabela);

            if (tipoArgumento != parametro.getTipo()) {
                String mensagem = String.format("'%s' na posição %d espera argumento do tipo %s mas recebeu do tipo %s",
                        parametro.getNome(), i + 1, parametro.getTipo(), tipoArgumento);
                throw new ErroSemantico(mensagem, chamada.getToken());
            }
        }

        return funcao.tipoRetorno;
    }

    private TipoVariavel analisarChamadaFuncao(ExpressaoChamadaFuncao chamada, DeclaracaoFuncao funcao, TabelaDeSimbolos tabela) {
        List<Expressao> argumentos = chamada.getArgumentos();
        if (argumentos.size() != funcao.getParametros().size()) {
            String mensagem = "Função '" + funcao.getNome() + "' espera receber " + funcao.getParametros().size()
                              + " argumento(s), mas recebeu " + argumentos.size();
            throw new ErroSemantico(mensagem, chamada.getToken());
        }

        for (int i = 0; i < argumentos.size(); i++) {
            Expressao argumento = argumentos.get(i);
            ParametroFuncao parametro = funcao.getParametros().get(i);
            ExpressaoIdentificador referenciaVetor = getReferenciaVetor(argumento, tabela);

            if (parametro.isVetor() && referenciaVetor == null) {
                String mensagem = "'" + parametro.getNome() + "' posição " + (i + 1) + " espera um vetor";
                throw new ErroSemantico(mensagem, chamada.getToken());
            }
            if (!parametro.isVetor() && referenciaVetor != null) {
                String mensagem = "'" + parametro.getNome() + "' posição " + (i + 1) + " não espera um vetor, mas recebeu '"
                                  + referenciaVetor.getIdentificador() + "'";
                throw new ErroSemantico(mensagem, chamada.getToken());
            }

            TipoVariavel tipoArgumento = parametro.isVetor()
                    ? analisarReferenciaVetor(argumento, tabela)
                    : analisarExpressao(argumento, tabela);

            if (tipoArgumento != parametro.getTipo().getTipo()) {
                String mensagem = "'" + parametro.getNome() + "' posição " + (i + 1) + " espera argumento do tipo "
                                  + parametro.getTipo().getTipo() + " mas recebeu do tipo " + tipoArgumento;
                throw new ErroSemantico(mensagem, chamada.getToken());
            }
        }

        return funcao.getTipoRetorno().getTipo();
    }

    // Se a expressão é o nome de um vetor sem índice (possivelmente entre parênteses), retorna seu identificador.
    // É a única forma de usar um vetor sem índice: como argumento para um parâmetro vetor
    private ExpressaoIdentificador getReferenciaVetor(Expressao expressao, TabelaDeSimbolos tabela) {
        while (expressao instanceof ExpressaoEntreParenteses entreParenteses) {
            expressao = entreParenteses.getExpressao();
        }

        if (expressao instanceof ExpressaoIdentificador identificador && identificador.getIndex() == null) {
            Simbolo simbolo = getSimbolo(identificador.getToken(), tabela, identificador.getIdentificador());
            if (simbolo instanceof SimboloVariavel variavel && variavel.isVetor()) {
                return identificador;
            }
        }

        return null;
    }

    // Anota o argumento que referencia um vetor inteiro: seu tipo é o tipo dos elementos
    private TipoVariavel analisarReferenciaVetor(Expressao expressao, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = switch (expressao) {
            case ExpressaoEntreParenteses entreParenteses -> analisarReferenciaVetor(entreParenteses.getExpressao(), tabela);
            case ExpressaoIdentificador identificador -> {
                SimboloVariavel variavel = (SimboloVariavel) getSimbolo(identificador.getToken(), tabela, identificador.getIdentificador());
                identificador.setSimbolo(variavel);
                yield variavel.getTipo();
            }
            default -> throw new BugCompilador("Referência a vetor inválida: " + expressao.codigoOriginal());
        };

        expressao.setTipo(tipo);
        return tipo;
    }

    private Simbolo getSimbolo(Token token, TabelaDeSimbolos tabela, String nome) {
        Simbolo simbolo = tabela.getSimbolo(nome);
        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nome, token);
        }
        return simbolo;
    }
}
