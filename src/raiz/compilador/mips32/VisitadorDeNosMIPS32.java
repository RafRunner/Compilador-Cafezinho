package src.raiz.compilador.mips32;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.DeclaracaoVariavelEmBloco;
import src.raiz.ast.expressoes.*;
import src.raiz.ast.expressoes.Expressao;
import src.raiz.compilador.FuncoesNativas;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.ModoGerador;
import src.raiz.compilador.VisitadorDeNos;
import src.raiz.compilador.tabeladesimbolos.*;
import src.raiz.erros.BugCompilador;
import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class VisitadorDeNosMIPS32 implements VisitadorDeNos {

    private final TabelaDeSimbolos tabelaGlobal;
    private final GeradorDeCodigo gerador;
    private final Programa programa;
    private final Set<String> labelsGeradas = new HashSet<>();
    private final Map<String, String> stringsParaLabels = new HashMap<>();
    private final Random random = new Random(System.currentTimeMillis());

    // Se estamos gerando código para uma função, ela está preenchida aqui
    private src.raiz.ast.declaracoes.DeclaracaoFuncao funcaoAtual = null;

    public VisitadorDeNosMIPS32(Programa programa, GeradorDeCodigo gerador) {
        this.programa = programa;
        this.gerador = gerador;
        this.tabelaGlobal = new TabelaDeSimbolos();
    }

    @Override
    public void visitarPorgrama() {
        visitarDeclaracaoFuncaoEVariaveis(programa.getDeclaracaoFuncoesEVariaveis());
        visitarBlocoPrograma(programa.getBlocoPrograma());
    }

    @Override
    public void visitarDeclaracaoFuncaoEVariaveis(src.raiz.ast.declaracoes.DeclaracaoFuncoesEVariaveis node) {
        for (src.raiz.ast.declaracoes.Declaracao declaracao : node.getDeclaracoesEmOrdem()) {
            if (declaracao instanceof src.raiz.ast.declaracoes.DeclaracaoDeVariavel declaracaoDeVariavel) {
                // Processar declarações de variáveis globais
                gerador.setModoAtual(ModoGerador.VARIAVEIS_GLOBAIS);

                for (Variavel var : declaracaoDeVariavel.getVariaveis()) {
                    String nomeResumido = gerarLabelUnico();
                    SimboloVariavelGlobal simbolo = new SimboloVariavelGlobal(var, nomeResumido);
                    tabelaGlobal.adicionaSimbolo(simbolo);

                    String nomeVariavel = simbolo.getNome();

                    // Gerar código para variável global
                    gerador.gerar(simbolo.getAlias() + ": .space " + getEspacoMemoriaVariavelGlobal(var) + " # " + nomeVariavel);
                }
            } else {
                gerador.setModoAtual(ModoGerador.FUNCAO);

                src.raiz.ast.declaracoes.DeclaracaoFuncao funcao = (src.raiz.ast.declaracoes.DeclaracaoFuncao) declaracao;
                String nomeFuncao = funcao.getNome();
                List<ParametroFuncao> parametros = funcao.getParametros();

                // Tabela que contêm as variáveis locais da função, incluindo os parâmetros de chamada
                TabelaDeSimbolos tabelaFuncao = tabelaGlobal.criaBlocoInterno();

                tabelaGlobal.adicionaSimbolo(new SimboloFuncao(funcao, tabelaFuncao));

                // Gerar etiqueta (label) da função
                gerador.gerar(nomeFuncao + ":");

                for (ParametroFuncao param : parametros) {
                    tabelaFuncao.adicionaSimbolo(new SimboloParametroFuncao(param, tabelaFuncao.getOffset()));
                    tabelaFuncao.alteraOffset(4);
                }

                // Guardando endereço de retorno no stack (espaço reservado na chamada)
                gerador.gerar("sw    $ra, " + tabelaFuncao.getOffset() + "($sp)");

                funcaoAtual = funcao;
                // Gerar código do corpo da função
                visitarEscopo(funcao.getCorpo(), tabelaFuncao);

                gerador.gerar("# fim função " + funcao.getNome() + "\n");
            }
        }
    }

    @Override
    public void visitarBlocoPrograma(src.raiz.ast.declaracoes.BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.gerar(".globl main");
        gerador.gerar("main:");

        funcaoAtual = null;
        visitarEscopo(blocoPrograma.getBlocoDeclaracoes(), tabelaGlobal);

        // Finalizar o programa
        gerador.gerar("li    $v0, 10 # finalizando programa"); // Syscall para saída
        gerador.gerar("syscall       # fim main");             // Executar syscall
    }

    // Marca um novo escopo, com nova tabela
    @Override
    public void visitarEscopo(src.raiz.ast.declaracoes.BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaDoEscopo) {
        for (src.raiz.ast.declaracoes.Declaracao declaracao : blocoDeclaracoes.getDeclaracoes()) {
            if (declaracao instanceof src.raiz.ast.declaracoes.DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco) {
                visitarDeclaracaoDeVariaveisEmBloco(declaracaoVariavelEmBloco, tabelaDoEscopo);
            } else if (declaracao instanceof Comando comando) {
                visitarComando(comando, tabelaDoEscopo);
            }
        }

        if (tabelaDoEscopo.getDiferencaOffset() != 0) {
            // Código para reajustar o stack pointer no final do escopo
            gerador.gerar("addiu $sp, $sp, " + tabelaDoEscopo.getDiferencaOffset() + " # reajusta stack bloco");
            tabelaDoEscopo.alteraOffset(-tabelaDoEscopo.getDiferencaOffset());
        }
    }

    private void visitarComando(Comando comando, TabelaDeSimbolos tabela) {
        switch (comando) {
            case ComandoBloco comandoBloco -> {
                TabelaDeSimbolos tabelaSubEscopo = tabela.criaBlocoInterno();
                visitarEscopo(comandoBloco.getDeclaracoes(), tabelaSubEscopo);
            }
            case ComandoEscreva comandoEscreva -> visitarComandoEscreva(comandoEscreva, tabela);
            case ComandoLeia comandoLeia -> visitarComandoLeia(comandoLeia, tabela);
            case ComandoNovalinha comandoNovalinha -> visitarComandoNovalinha(comandoNovalinha);
            case ComandoRetorno comandoRetorno -> visitarComandoRetorno(comandoRetorno, tabela);
            case ComandoSe comandoSe -> visitarComandoSe(comandoSe, tabela);
            case ComandoEnquanto comandoEnquanto -> visitarComandoEnquanto(comandoEnquanto, tabela);
            case ComandoComExpressao comandoComExpressao -> {
                Expressao expressao = comandoComExpressao.getExpressao();
                visitarExpressao(expressao, tabela);
                // Limpando efeito colateral
                desempilharEmS0(tabela);
            }
            default -> throw new BugCompilador("Tipo de comando não identificado " + comando.getClass());
        }

    }

    // Após executar uma expressão, seu resultado estará no topo do stack
    @Override
    public TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo) {
        return switch (expressao) {
            case ExpressaoEntreParenteses expressaoEntre -> visitarExpressao(expressaoEntre.getExpressao(), tabelaDoEscopo);
            case ExpressaoIdentificador expressaoIdentificador -> visitaIdentificador(expressaoIdentificador, tabelaDoEscopo);
            case ExpressaoInteiroLiteral intLiteral -> visitarExpressaoInteiroLiteral(intLiteral, tabelaDoEscopo);
            case ExpressaoFlutuanteLiteral flutLiteral -> visitarExpressaoFlutuanteLiteral(flutLiteral, tabelaDoEscopo);
            case ExpressaoCaractereLiteral carVirtual -> visitarExpressaoCaractereLiteral(carVirtual, tabelaDoEscopo);
            case ExpressaoStringLiteral stringLiteral -> visitarExpressaoStringLiteral(stringLiteral, tabelaDoEscopo);
            case ExpressaoAtribuicao expressaoAtribuicao -> visitarExpressaoAtribuicao(expressaoAtribuicao, tabelaDoEscopo);
            case ExpressaoMais expressaoMais -> visitarExpressaoSoma(expressaoMais, tabelaDoEscopo);
            case ExpressaoMenos expressaoMenos -> visitarExpressaoSubtracao(expressaoMenos, tabelaDoEscopo);
            case ExpressaoVezes expressaoVezes -> visitarExpressaoVezes(expressaoVezes, tabelaDoEscopo);
            case ExpressaoDivisao expressaoDivisao -> visitarExpressaoDivisao(expressaoDivisao, tabelaDoEscopo);
            case ExpressaoE expressaoE -> visitarExpressaoE(expressaoE, tabelaDoEscopo);
            case ExpressaoOu expressaoOu -> visitarExpressaoOu(expressaoOu, tabelaDoEscopo);
            case ExpressaoIgual expressaoIgual -> visitarExpressaoIgual(expressaoIgual, tabelaDoEscopo);
            case ExpressaoDiferente expressaoDiferente -> visitarExpressaoDiferente(expressaoDiferente, tabelaDoEscopo);
            case ExpressaoMaior expressaoMaior -> visitarExpressaoMaior(expressaoMaior, tabelaDoEscopo);
            case ExpressaoMaiorIgual expressaoMaiorIgual -> visitarExpressaoMaiorIgual(expressaoMaiorIgual, tabelaDoEscopo);
            case ExpressaoMenor expressaoMenor -> visitarExpressaoMenor(expressaoMenor, tabelaDoEscopo);
            case ExpressaoMenorIgual expressaoMenorIgual -> visitarExpressaoMenorIgual(expressaoMenorIgual, tabelaDoEscopo);
            case ExpressaoResto expressaoResto -> visitarExpressaoResto(expressaoResto, tabelaDoEscopo);
            case ExpressaoTernaria expressaoTernaria -> visitarExpressaoTernaria(expressaoTernaria, tabelaDoEscopo);
            case ExpressaoNegativo expressaoNegativo -> visitarExpressaoNegativo(expressaoNegativo, tabelaDoEscopo);
            case ExpressaoNegacao expressaoNegacao -> visitarExpressaoNegacao(expressaoNegacao, tabelaDoEscopo);
            case ExpressaoLeia expressaoLeia -> visitarExpressaoLeia(expressaoLeia, tabelaDoEscopo);
            case ExpressaoChamadaFuncao chamadaFuncao -> visitaExpressaoChamadaFuncao(chamadaFuncao, tabelaDoEscopo);
            default -> throw new BugCompilador("Tipo de expressão não identificada " + expressao.getClass());
        };
    }

    // Aqui temos somente variáveis locais
    @Override
    public void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco) {
        for (src.raiz.ast.declaracoes.DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                tabelaBloco.adicionaSimbolo(new SimboloVariavelLocal(var, tabelaBloco.getOffset()));
                tabelaBloco.alteraOffset(4);

                gerador.gerar("addiu $sp, $sp, -4 # reservando espaço variável " + var.getNome());
                if (var.isVetor()) {
                    // Aloca o vetor e guarda o endereço no stack no espaço que acabou de ser reservado
                    alocaVetor(var);
                }
            }
        }
    }

    @Override
    public TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        String nomeVariavel = identificador.getIdentificador();

        // Verifique se a variável existe
        Simbolo<?> simbolo = getSimbolo(identificador.getToken(), tabela, nomeVariavel);

        if (simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO || simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO_NATIVA) {
            throw new ErroSemantico(nomeVariavel + " é uma função, deve ser invocada", identificador.getToken());
        }

        Expressao index = identificador.getIndex();
        if (!simbolo.isVetor() && index != null) {
            throw new ErroSemantico("Variável " + nomeVariavel + " não é um vetor, não pode ser indexada", identificador.getToken());
        }

        // Checar se é uma variável local ou global
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();
            TipoVariavel tipoVariavel = variavel.getTipo().getTipo();

            if (variavel.isVetor() && index != null) {
                visitarExpressaoIndex(identificador, tabela);
                leVariavelGlobalVetorIndexado(variavelGlobal, tabela);
            } else {
                leVariavelGlobal(variavelGlobal, tipoVariavel, tabela);
            }

            return tipoVariavel;
        } else {
            VariavelNoStack variavelLocal;
            if (simbolo.getTipoSimbolo() == TipoSimbolo.PARAMETRO_FUNCAO) {
                variavelLocal = ((SimboloParametroFuncao) simbolo).getVariavelNoStack();
            } else {
                variavelLocal = ((SimboloVariavelLocal) simbolo).getVariavelNoStack();
            }

            if (variavelLocal.isVetor() && index != null) {
                visitarExpressaoIndex(identificador, tabela);
                leVariavelLocalVetorIndexado(variavelLocal, tabela);
            } else {
                gerador.gerar("lw    $s0, " + tabela.getOffsetStack(variavelLocal.getOffset()) + "($sp) # lendo variável local " + nomeVariavel);
                empilharS0(tabela);
                gerador.gerar("# fim lendo variável local " + nomeVariavel);
            }

            return variavelLocal.getTipoVariavel();
        }
    }

    @Override
    public TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela) {
        int valor = expressao.getConteudo();
        // Carregar valor no registrador $s0
        gerador.gerar("li    $s0, " + valor + " # empilhando inteiro literal " + valor);
        empilharS0(tabela);

        return TipoVariavel.INTEIRO;
    }

    @Override
    public TipoVariavel visitarExpressaoFlutuanteLiteral(ExpressaoFlutuanteLiteral expressao, TabelaDeSimbolos tabela) {
        float valor = expressao.getConteudo();
        // Carrega o valor como um int no padrão IEEE 754
        gerador.gerar("li    $s0, " + Float.floatToRawIntBits(valor) + " # empilhando flutuante literal " + valor);
        empilharS0(tabela);

        return TipoVariavel.FLUTUANTE;
    }

    @Override
    public TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela) {
        char valor = expressao.getConteudo();
        // Carregar valor ASCII no registrador $s0
        gerador.gerar("li    $s0, " + (int) valor + " # empilhando caractere literal " + valor);
        empilharS0(tabela);

        return TipoVariavel.CARACTERE;
    }

    @Override
    public TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela) {
        String valor = expressao.getConteudo();
        String label = stringsParaLabels.get(valor);

        if (label == null) {
            // Função auxiliar para gerar um label único para a string
            label = gerarLabelUnico();
            // Adicionar string na seção .data
            gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");
            stringsParaLabels.put(valor, label);
        }

        // Carregar endereço da string e empilhá-lo no stack
        gerador.gerar("la    $s0, " + label + " # empilhando endereço de string literal"); // Carregar endereço no registrador $s0
        empilharS0(tabela); // Empilhar o endereço no stack

        return TipoVariavel.STRING;
    }

    @Override
    public void visitarComandoNovalinha(ComandoNovalinha comandoNovalinha) {
        gerador.gerar("li    $v0, 11 # novaLinha"); // Syscall para imprimir char
        gerador.gerar("li    $a0, 10");             // ACII do \n
        gerador.gerar("syscall       # fim novaLinha");
    }

    @Override
    public TipoVariavel visitarExpressaoSoma(ExpressaoMais expressaoMais, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaNumerica(expressaoMais, tabela, "soma", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("add.s $f0, $f0, $f1 # soma $f0 e $f1, resultado em $f0");
            } else {
                gerador.gerar("add   $t0, $t0, $t1 # soma $t0 e $s1, resultado em $t0");
            }

            return tipo;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoSubtracao(ExpressaoMenos expressaoMenos, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaNumerica(expressaoMenos, tabela, "subtração", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("sub.s $f0, $f0, $f1 # subtrai $f0 e $f1, resultado em $f0");
            } else {
                gerador.gerar("sub   $t0, $t0, $t1 # subtrai $t1 de $t0, resultado em $t0");
            }

            return tipo;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoVezes(ExpressaoVezes expressaoVezes, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaNumerica(expressaoVezes, tabela, "multiplicação", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("mul.s $f0, $f0, $f1 # multiplica $f0 e $f1, resultado em $f0");
            } else {
                gerador.gerar("mul   $t0, $t0, $t1 # multiplica $t0 por $t1, resultado em $t0");
            }

            return tipo;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoDivisao(ExpressaoDivisao expressaoDivisao, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaNumerica(expressaoDivisao, tabela, "divisão", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("div.s $f0, $f0, $f1 # multiplica $f0 e $f1, resultado em $f0");
            } else {
                gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
                gerador.gerar("mflo  $t0      # move o resultado da divisão para $t0");
            }

            return tipo;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoResto(ExpressaoResto expressaoResto, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaInt(expressaoResto, tabela, "resto", () -> {
            gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
            gerador.gerar("mfhi  $t0      # move o resto da divisão para $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoE(ExpressaoE expressaoE, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaInt(expressaoE, tabela, "e", () ->
                gerador.gerar("and   $t0, $t0, $t1 # operação lógica E entre $t0 e $t1, resultado em $t0")
        );
    }

    @Override
    public TipoVariavel visitarExpressaoOu(ExpressaoOu expressaoOu, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaInt(expressaoOu, tabela, "ou", () ->
                gerador.gerar("or    $t0, $t0, $t1 # operação lógica Ou entre $t0 e $t1, resultado em $t0")
        );
    }

    @Override
    public TipoVariavel visitarExpressaoIgual(ExpressaoIgual expressaoIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoIgual, tabela, "igual", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.eq.s $f0, $f1 # verifica se $f0 == $f1");
                guardaComparacaoFloatEmT0(false);
            } else {
                gerador.gerar("sub   $t0, $t0, $t1 # subtrai $t1 de $t0");
                gerador.gerar("sltiu $t0, $t0, 1   # define $t0 como 1 se $t0 era 0 (iguais), ou 0 caso contrário");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoDiferente(ExpressaoDiferente expressaoDiferente, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoDiferente, tabela, "diferente", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.eq.s $f0, $f1 # verifica se $f0 != $f1");
                guardaComparacaoFloatEmT0(true);
            } else {
                gerador.gerar("sub   $t0, $t0, $t1   # subtrai $t1 de $t0");
                gerador.gerar("sltu  $t0, $zero, $t0 # define $t0 como 1 se são diferentes, 0 se iguais");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMaior(ExpressaoMaior expressaoMaior, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoMaior, tabela, "maior", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.lt.s $f1, $f0 # verifica se $f0 > $f1");
                guardaComparacaoFloatEmT0(false);
            } else {
                gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0, resultado em $t0");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMaiorIgual(ExpressaoMaiorIgual expressaoMaiorIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoMaiorIgual, tabela, "maior igual", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.le.s $f1, $f0 # verifica se $f0 >= $f1");
                guardaComparacaoFloatEmT0(false);
            } else {
                gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1");
                gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'maior ou igual'");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMenor(ExpressaoMenor expressaoMenor, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoMenor, tabela, "menor", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.lt.s $f0, $f1 # verifica se $f0 < $f1");
                guardaComparacaoFloatEmT0(false);
            } else {
                gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1, resultado em $t0");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMenorIgual(ExpressaoMenorIgual expressaoMenorIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinariaMesmoTipo(expressaoMenorIgual, tabela, "menor igual", (tipo) -> {
            if (tipo == TipoVariavel.FLUTUANTE) {
                gerador.gerar("c.le.s $f0, $f1 # verifica se $f0 <= $f1");
                guardaComparacaoFloatEmT0(false);
            } else {
                gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0");
                gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'menor ou igual'");
            }

            return TipoVariavel.INTEIRO;
        });
    }

    private void guardaComparacaoFloatEmT0(boolean inverter) {
        String labelFalso = gerarLabelUnico();
        String labelFim = gerarLabelUnico();

        gerador.gerar("bc1f  " + labelFalso);
        gerador.gerar("li    $t0, " + (inverter ? 0 : 1));
        gerador.gerar("j     " + labelFim);
        gerador.gerar(labelFalso + ":");
        gerador.gerar("li    $t0, " + (inverter ? 1 : 0));
        gerador.gerar(labelFim + ":");
    }

    @Override
    public TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela) {
        String nomeVariavel = expressaoAtribuicao.getIdentificador().getIdentificador();
        Simbolo<?> simbolo = getSimbolo(expressaoAtribuicao.getToken(), tabela, nomeVariavel);

        if (simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO || simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO_NATIVA) {
            throw new ErroSemantico(nomeVariavel + " é uma função, não pode ter valor atribuído", expressaoAtribuicao.getToken());
        }

        Expressao index = expressaoAtribuicao.getIdentificador().getIndex();
        if (!simbolo.isVetor() && index != null) {
            throw new ErroSemantico("Variável " + nomeVariavel + " não é um vetor, não pode ser indexada", expressaoAtribuicao.getToken());
        }

        Expressao ladoDireito = expressaoAtribuicao.getExpressaoLadoDireito();
        TipoVariavel tipoDireito = visitarExpressao(ladoDireito, tabela);

        if (tipoDireito != simbolo.getTipoVariavel()) {
            throw new ErroSemantico(
                    "A variável " + nomeVariavel + " é do tipo " + simbolo.getTipoVariavel() + " e não pode receber valor do tipo " + tipoDireito,
                    expressaoAtribuicao.getToken()
            );
        }

        // Não desempilhamos aqui pois desejamos que o valor continue no stack
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            // Atribuir valor a uma variável global
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();

            if (variavel.isVetor() && index != null) {
                gerador.gerar("la    $t0, " + variavelGlobal.getAlias() + " # carrega endereço do vetor global em $t0 " + variavel.getNome());
                atribuiAVetor(expressaoAtribuicao, tipoDireito, tabela);
            } else {
                gerador.gerar("la    $t1, " + variavelGlobal.getAlias()); // Carregar endereço da variável global

                // Carregar valor do topo do stack e armazenar valor na variável global
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável global " + variavel.getNome());
                gerador.gerar("sw    $t0, 0($t1)");
            }
        } else {
            // Atribuir valor a uma variável local
            VariavelNoStack variavelLocal;
            if (simbolo.getTipoSimbolo() == TipoSimbolo.PARAMETRO_FUNCAO) {
                variavelLocal = ((SimboloParametroFuncao) simbolo).getVariavelNoStack();
            } else {
                variavelLocal = ((SimboloVariavelLocal) simbolo).getVariavelNoStack();
            }
            int offset = tabela.getOffsetStack(variavelLocal.getOffset());

            if (variavelLocal.isVetor() && index != null) {
                gerador.gerar("lw    $t0, " + offset + "($sp) # carrega endereço do vetor em $t0 " + variavelLocal.getNome());
                atribuiAVetor(expressaoAtribuicao, tipoDireito, tabela);
            } else {
                // Carregar valor do topo do stack e armazenar valor float na variável local
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável local " + variavelLocal.getNome());
                gerador.gerar("sw    $t0, " + offset + "($sp)");
            }
        }

        return tipoDireito;
    }

    // Endereço do vetor deve estar no registrador $t0
    private void atribuiAVetor(ExpressaoAtribuicao expressaoAtribuicao, TipoVariavel tipo, TabelaDeSimbolos tabela) {
        empilhar(tabela, RegistradoresMIPS32.T0);

        int tamanhoElemento = getEspacoMemoria(tipo);
        visitarExpressaoIndex(expressaoAtribuicao.getIdentificador(), tabela);
        desempilharEmS0(tabela); // Desempilha o index
        desempilhar(tabela, RegistradoresMIPS32.T0); // Desempilha o endereço

        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t0, $t1, $t0 # soma o endereço com o index * tamanho");

        gerador.gerar("lw    $t3, 0($sp) # lê o valor a ser armazenado");

        if (tipo == TipoVariavel.CARACTERE) {
            gerador.gerar("sb    $t3, 0($t0) # armazena byte");
        } else {
            gerador.gerar("sw    $t3, 0($t0) # armazena int");
        }
    }

    @Override
    public TipoVariavel visitarExpressaoTernaria(ExpressaoTernaria expressaoTernaria, TabelaDeSimbolos tabela) {
        gerador.gerar("# inicio expressão ternária");

        // Avaliar condição
        visitarExpressao(expressaoTernaria.getCondicao(), tabela);
        desempilharEmS0(tabela); // Carrega o resultado da condição em $t0

        String labelFalso = "ternarioFalso_" + gerarLabelUnico();
        String labelFim = "ternarioFim_" + gerarLabelUnico();

        // Verifica condição e pula para o labelFalso se for 0 (falso)
        gerador.gerar("beqz  $s0, " + labelFalso);

        // Avaliar expressão 'se' e empilhar resultado se condição for verdadeira
        TipoVariavel tipoSe = visitarExpressao(expressaoTernaria.getSe(), tabela);
        gerador.gerar("j     " + labelFim); // Pula para o fim após avaliar 'se'

        // Label e avaliação para expressão 'senao'
        gerador.gerar(labelFalso + ":");
        TipoVariavel tipoSenao = visitarExpressao(expressaoTernaria.getSenao(), tabela);

        gerador.gerar(labelFim + ": # fim expressão ternária");

        // Garante que ambos os ramos da expressão ternária retornem o mesmo tipo
        if (tipoSe != tipoSenao) {
            throw new ErroSemantico("Os ramos da expressão ternária devem retornar o mesmo tipo.", expressaoTernaria.getToken());
        }

        return tipoSe; // Retorna o tipo dos ramos da expressão ternária
    }

    @Override
    public TipoVariavel visitarExpressaoNegacao(ExpressaoNegacao expressaoNegacao, TabelaDeSimbolos tabela) {
        return visitarExpressaoUnaria(expressaoNegacao, tabela, "negação", (tipoVariavel) -> {
            if (tipoVariavel != TipoVariavel.INTEIRO) {
                throw new ErroSemantico("Não se pode aplicar negação a valores não booleanos (inteiros)", expressaoNegacao.getToken());
            }
            gerador.gerar("seq   $t0, $t0, $zero # inverte o valor booleano, 1 se $t0 é 0, 0 caso contrário");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoNegativo(ExpressaoNegativo expressaoNegativo, TabelaDeSimbolos tabela) {
        return visitarExpressaoUnaria(expressaoNegativo, tabela, "negativo", (tipoVariavel) -> {
            if (tipoVariavel != TipoVariavel.INTEIRO && tipoVariavel != TipoVariavel.FLUTUANTE) {
                throw new ErroSemantico("Não se pode aplicar negativo a valores não numéricos", expressaoNegativo.getToken());
            }

            if (tipoVariavel == TipoVariavel.FLUTUANTE) {
                gerador.gerar("neg.s $f0, $f0 # inverte o sinal do número");
            } else {
                gerador.gerar("sub   $t0, $zero, $t0 # inverte o sinal do número");
            }
        });
    }

    @Override
    public void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = visitarExpressao(comandoEscreva.getExpressao(), tabela);

        gerador.gerar("# inicio escreva");

        switch (tipo) {
            case INTEIRO -> gerador.gerar("li    $v0, 1");  // Syscall para imprimir inteiro
            case CARACTERE -> gerador.gerar("li    $v0, 11"); // Syscall para imprimir char
            case STRING -> gerador.gerar("li    $v0, 4");  // Syscall para imprimir string
            case FLUTUANTE -> gerador.gerar("li    $v0, 2");  // Syscall para imprimir float
        }

        // Carregar do topo do stack
        if (tipo == TipoVariavel.FLUTUANTE) {
            gerador.gerar("lwc1  $f12, 0($sp)");
        } else {
            gerador.gerar("lw    $a0, 0($sp)");
        }
        gerador.gerar("syscall # fim escreva");

        desempilharEmS0(tabela);
    }

    @Override
    public TipoVariavel visitaExpressaoChamadaFuncao(ExpressaoChamadaFuncao chamada, TabelaDeSimbolos tabela) {
        String nomeFuncao = chamada.getNomeFuncao();
        gerador.gerar("# inicio chamada da função " + nomeFuncao);

        Simbolo<?> simbolo = tabela.getSimbolo(nomeFuncao);
        if (simbolo == null) {
            throw new ErroSemantico("A função " + nomeFuncao + " não foi declarada", chamada.getToken());
        }
        if (simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO_NATIVA) {
            SimboloFuncaoNativa funcaoNativa = (SimboloFuncaoNativa) simbolo;
            return visitarFuncaoNativa(chamada, funcaoNativa.getNoSintatico().getFuncaoNativa(), tabela);
        }
        if (simbolo.getTipoSimbolo() != TipoSimbolo.FUNCAO) {
            throw new ErroSemantico(nomeFuncao + " não é uma função", chamada.getToken());
        }
        SimboloFuncao funcaoSimbolo = (SimboloFuncao) simbolo;
        src.raiz.ast.declaracoes.DeclaracaoFuncao funcao = funcaoSimbolo.getNoSintatico();

        if (chamada.getArgumentos().size() != funcao.getParametros().size()) {
            throw new ErroSemantico(
                    "Função " + nomeFuncao + " espera receber " + funcao.getParametros().size() + " argumento(s), mas recebeu " + chamada.getArgumentos().size(),
                    chamada.getToken()
            );
        }

        // Prólogo da função
        gerador.gerar("addiu $sp, $sp, -4 # reservando espaço para o endereço de retorno da função");
        tabela.alteraOffset(4); // Reservando espaço do endereço de retorno

        for (int i = 0; i < chamada.getArgumentos().size(); i++) {
            Expressao argumento = chamada.getArgumentos().get(i);
            TipoVariavel tipoArgumento = visitarExpressao(argumento, tabela);
            ParametroFuncao parametro = funcao.getParametros().get(i);

            if (tipoArgumento != parametro.getTipo().getTipo()) {
                throw new ErroSemantico(
                        parametro.getNome() + " posição " + (i + 1) + " espera argumento do tipo " + parametro.getTipo().getTipo() + " mas recebeu do tipo " + tipoArgumento,
                        chamada.getToken()
                );
            }
        }

        gerador.gerar("jal    " + nomeFuncao + " # chama a função " + nomeFuncao); // Chamada da função
        tabela.alteraOffset(-4 * (chamada.getArgumentos().size() + 1)); // Limpando espaço dos argumentos + endereço retorno
        empilhar(tabela, RegistradoresMIPS32.V0); // armazena o resultado da função no stack

        gerador.gerar("# fim chamada da função " + nomeFuncao);

        return funcao.getTipoRetorno().getTipo();
    }

    @Override
    public void visitarComandoRetorno(ComandoRetorno comandoRetorno, TabelaDeSimbolos tabela) {
        gerador.gerar("# inicio comando retorno");

        TipoVariavel tipoRetorno = visitarExpressao(comandoRetorno.getExpressao(), tabela);

        if (funcaoAtual == null) {
            // Caso estejamos na função main
            desempilhar(tabela, RegistradoresMIPS32.A0); // Carrega o valor de retorno em $a0
            gerador.gerar("li    $v0, 17 # syscall para finalizar o programa com código de retorno");
            gerador.gerar("syscall       # fim main");
        } else {
            // Verifica se o tipo de retorno é compatível com o tipo de retorno da função
            if (tipoRetorno != funcaoAtual.getTipoRetorno().getTipo()) {
                throw new ErroSemantico(
                        "Tipo de retorno incompatível. Esperado: " + funcaoAtual.getTipoRetorno().getTipo() + ", recebido: " + tipoRetorno,
                        comandoRetorno.getToken()
                );
            }
            desempilhar(tabela, RegistradoresMIPS32.V0); // Carrega o valor de retorno em $v0

            finalizarFuncao(tabela, funcaoAtual.getNome());
        }

        gerador.gerar("# fim comando retorno");
    }

    private void finalizarFuncao(TabelaDeSimbolos tabelaFuncao, String nomeFuncao) {
        // Epílogo da função
        // Código para reajustar o stack pointer ao retornar da função: variáveis locais + parâmetros
        if (tabelaFuncao.getOffset() != 0) {
            gerador.gerar("addiu $sp, $sp, " + tabelaFuncao.getOffset() + " # reajusta stack bloco retorno");
        }
        gerador.gerar("lw    $ra, 0($sp) # lê endereço de retorno do stack");
        gerador.gerar("addiu $sp, $sp, 4 # volta o stack para limpar o endereço de retorno");
        gerador.gerar("jr    $ra         # retornando da função " + nomeFuncao);
    }

    @Override
    public void visitarComandoSe(ComandoSe comandoSe, TabelaDeSimbolos tabela) {
        String labelFalso = "seFalse_" + gerarLabelUnico();
        String labelFim = "seFim_" + gerarLabelUnico();

        gerador.gerar("# inicio comando se");

        // Avalia a expressão condicional
        visitarExpressao(comandoSe.getSe(), tabela);
        desempilhar(tabela, RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFalso se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFalso);

        // Executa o comando de consequência
        visitarComando(comandoSe.getConsequencia(), tabela);

        // Pula para o final se um bloco alternativo existir
        if (comandoSe.getAlternativa() != null) {
            gerador.gerar("j     " + labelFim);
        }

        // Label para o bloco alternativo/final se não houver senao
        gerador.gerar(labelFalso + ":");
        if (comandoSe.getAlternativa() != null) {
            visitarComando(comandoSe.getAlternativa(), tabela);
            // Label para o final do comando se
            gerador.gerar(labelFim + ":");
        }

        gerador.gerar("# fim comando se");
    }

    @Override
    public void visitarComandoEnquanto(ComandoEnquanto comandoEnquanto, TabelaDeSimbolos tabela) {
        String labelInicio = "enquantoInicio_" + gerarLabelUnico();
        String labelFim = "enquantoFim_" + gerarLabelUnico();

        gerador.gerar("# inicio comando enquanto");
        gerador.gerar(labelInicio + ":");

        // Avalia a expressão condicional
        visitarExpressao(comandoEnquanto.getCondicional(), tabela);
        desempilhar(tabela, RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFim se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFim);

        // Executa o comando dentro do loop
        visitarComando(comandoEnquanto.getComando(), tabela);

        // Retorna ao início para avaliar a condição novamente
        gerador.gerar("j     " + labelInicio);

        // Label para o final do loop
        gerador.gerar(labelFim + ":");
        gerador.gerar("# fim comando enquanto");
    }

    @Override
    public void visitarComandoLeia(ComandoLeia comandoLeia, TabelaDeSimbolos tabela) {
        gerador.gerar("# inicio comando leia");

        ExpressaoIdentificador identificador = comandoLeia.getExpressaoIdentificador();
        Simbolo<?> simbolo = getSimbolo(identificador.getToken(), tabela, identificador.getIdentificador());

        TipoVariavel tipo = simbolo.getTipoVariavel();

        // Simulando uma atribuição, pois é isso que precisamos fazer para dar valor à variável
        ExpressaoAtribuicao expressaoAtribuicao = new ExpressaoAtribuicao(
                comandoLeia.getToken(),
                comandoLeia.getExpressaoIdentificador(),
                new ExpressaoLeia(comandoLeia.getToken(), tipo)
        );
        visitarExpressaoAtribuicao(expressaoAtribuicao, tabela);
        // Desempilha, pois é um comando
        desempilharEmS0(tabela);
    }

    @Override
    public void visitarFuncaoRand(TabelaDeSimbolos tabela) {
        gerador.gerarFuncaoNativa(FuncoesNativas.RAND);

        desempilhar(tabela, RegistradoresMIPS32.A0);
        gerador.gerar("jal   " + FuncoesNativas.RAND.nome + " # chama a função nativa");
        empilhar(tabela, RegistradoresMIPS32.V0);
    }

    @Override
    public void visitarFuncaoPiso(TabelaDeSimbolos tabela) {
        gerador.gerarFuncaoNativa(FuncoesNativas.PISO);

        desempilhar(tabela, RegistradoresMIPS32.F12);
        gerador.gerar("jal   " + FuncoesNativas.PISO.nome + " # chama a função nativa");
        empilhar(tabela, RegistradoresMIPS32.V0);
    }

    // Expressão que lê o que foi digitado e empilha
    private TipoVariavel visitarExpressaoLeia(ExpressaoLeia expressaoLeia, TabelaDeSimbolos tabela) {
        if (expressaoLeia.getTipoVariavel() == TipoVariavel.INTEIRO) {
            gerador.gerar("li    $v0, 5 # syscall para ler inteiro");
            gerador.gerar("syscall");
        } else if (expressaoLeia.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("li    $v0, 12 # syscall para ler caractere");
            gerador.gerar("syscall");
        } else {
            throw new ErroSemantico(
                    "Tipo " + expressaoLeia.getTipoVariavel() + " não suportado para leitura",
                    expressaoLeia.getToken()
            );
        }

        empilhar(tabela, RegistradoresMIPS32.V0);

        gerador.gerar("# fim comando leia");

        return expressaoLeia.getTipoVariavel();
    }

    private Simbolo<?> getSimbolo(Token token, TabelaDeSimbolos tabela, String nomeVariavel) {
        Simbolo<?> simbolo = tabela.getSimbolo(nomeVariavel);
        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nomeVariavel, token);
        }
        return simbolo;
    }

    private int getEspacoMemoria(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, STRING -> 4; // int tem 4 bytes -> 32 bits. Uma string é um ponteiro
            case CARACTERE -> 1; // um caractere é um byte
            case FLUTUANTE -> 4; // os floats são de precissão única
        };
    }

    private int getEspacoMemoriaVariavelGlobal(Variavel variavel) {
        int tamanhoBase = getEspacoMemoria(variavel.getTipo().getTipo());

        if (variavel.isVetor()) {
            return tamanhoBase * variavel.getTamanhoVetor(); // É vetor
        } else {
            return tamanhoBase; // não é vetor
        }
    }

    // Assumimos que foi criado espaço no stack para armazenar o endereço do vetor
    private void alocaVetor(Variavel variavel) {
        int tamanho = getEspacoMemoria(variavel.getTipo().getTipo()) * variavel.getTamanhoVetor();

        gerador.gerar("li    $v0, 9 # aloca vetor " + variavel.getNome()); // Syscall número 9 para alocar memória
        gerador.gerar("li    $a0, " + tamanho); // Carregar tamanho do array em $a0
        gerador.gerar("syscall");               // Executar syscall
        gerador.gerar("move  $s0, $v0");        // Endereço base do array alocado está agora no registrador $s0
        // Guarda o endereço do vetor no stack
        gerador.gerar("sw    $s0, 0($sp) # fim aloca vetor " + variavel.getNome());
    }

    private void leVariavelGlobal(SimboloVariavelGlobal variavelGlobal, TipoVariavel tipo, TabelaDeSimbolos tabela) {
        gerador.gerar("la    $s0, " + variavelGlobal.getAlias() + " # lendo variável global " + variavelGlobal.getNome()); // carrega endereço da variável em $s0

        if (!variavelGlobal.getNoSintatico().isVetor()) {
            // carrega o valor no endereço no registrador $s0 ou $f0 se float
            if (tipo == TipoVariavel.CARACTERE) {
                gerador.gerar("lb    $s0, 0($s0)");
                empilharS0(tabela);
            } else if (tipo == TipoVariavel.INTEIRO) {
                gerador.gerar("lw    $s0, 0($s0)");
                empilharS0(tabela);
            } else {
                gerador.gerar("lwc1  $f0, 0($s0)");
                empilhar(tabela, RegistradoresMIPS32.F0);
            }
        }

        gerador.gerar("# fim lendo variável global " + variavelGlobal.getNome());
    }

    private void leVariavelGlobalVetorIndexado(SimboloVariavelGlobal variavelGlobal, TabelaDeSimbolos tabela) {
        int tamanhoElemento = getEspacoMemoria(variavelGlobal.getTipoVariavel());

        // Carregar o endereço base do vetor global
        gerador.gerar("la    $t0, " + variavelGlobal.getAlias() + " # lendo vetor global indexado " + variavelGlobal.getNome());

        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(tabela);

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor e empilhar no stack
        if (variavelGlobal.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("lb    $s0, 0($t1)");
            empilharS0(tabela);
        } else if (variavelGlobal.getTipoVariavel() == TipoVariavel.INTEIRO) {
            gerador.gerar("lw    $s0, 0($t1)");
            empilharS0(tabela);
        } else {
            gerador.gerar("lwc1  $f0, 0($t1)");
            empilhar(tabela, RegistradoresMIPS32.F0);
        }

        gerador.gerar("# fim lendo vetor global indexado " + variavelGlobal.getNome());
    }

    private void leVariavelLocalVetorIndexado(VariavelNoStack variavelLocal, TabelaDeSimbolos tabela) {
        int offset = variavelLocal.getOffset();
        int tamanhoElemento = getEspacoMemoria(variavelLocal.getTipoVariavel());

        gerador.gerar("# lendo vetor local indexado " + variavelLocal.getNome());

        gerador.gerar("lw    $t0, " + tabela.getOffsetStack(offset) + "($sp) # lendo endereço do vetor");
        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(tabela);
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t0, $t0, $t1");

        if (variavelLocal.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("lb    $s0, 0($t0) # lê byte");
            empilharS0(tabela);
        } else {
            gerador.gerar("lw    $s0, 0($t0) # lê inteiro");
            empilharS0(tabela);
        }

        gerador.gerar("# fim lendo vetor local indexado " + variavelLocal.getNome());
    }

    private void empilharS0(TabelaDeSimbolos tabela) {
        empilhar(tabela, RegistradoresMIPS32.S0);
    }

    private void empilhar(TabelaDeSimbolos tabela, RegistradoresMIPS32 registrador) {
        gerador.gerar("addiu $sp, $sp, -4"); // Aloca espaço no stack

        // Guarda a variável no stack
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("swc1  " + registrador.getNome() + ", 0($sp)");
        } else {
            gerador.gerar("sw    " + registrador.getNome() + ", 0($sp)");
        }

        tabela.alteraOffset(4);
    }

    private void desempilharEmS0(TabelaDeSimbolos tabela) {
        desempilhar(tabela, RegistradoresMIPS32.S0);
    }

    private void desempilhar(TabelaDeSimbolos tabela, RegistradoresMIPS32 registrador) {
        // Guarda o topo do stack no registrador
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("lwc1  " + registrador.getNome() + ", 0($sp)");
        } else {
            gerador.gerar("lw    " + registrador.getNome() + ", 0($sp)");
        }
        gerador.gerar("addiu $sp, $sp, 4"); // Desaloca espaço no stack

        tabela.alteraOffset(-4);
    }

    private String gerarLabelUnico() {
        int tamanho = 20;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tamanho; i++) {
            // Decide aleatoriamente se será uma letra maiúscula ou minúscula
            int valorAscii = random.nextInt(52);
            char letraAleatoria;
            if (valorAscii > 25) {
                // Valor ASCII para letras maiúsculas (65 até 90)
                letraAleatoria = (char) (valorAscii - 26 + 65);
            } else {
                // Valor ASCII para letras minúsculas (97 até 122)
                letraAleatoria = (char) (valorAscii + 97);
            }
            sb.append(letraAleatoria);
        }

        String label = sb.toString();

        // Evitando labels duplicadas
        if (!labelsGeradas.add(label)) {
            return gerarLabelUnico();
        }

        return label;
    }

    private void visitarExpressaoIndex(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        TipoVariavel tipoVariavel = visitarExpressao(identificador.getIndex(), tabela);
        if (tipoVariavel != TipoVariavel.INTEIRO) {
            throw new ErroSemantico("Expressão de index deve ser um inteiro!", identificador.getToken());
        }
    }

    private TipoVariavel visitarExpressaoBinariaMesmoTipo(
            ExpressaoBinaria expressaoBinaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Function<TipoVariavel, TipoVariavel> operacaoEspecifica
    ) {
        return visitarExpressaoBinaria(expressaoBinaria, tabela, nomeOperacao, (tipoEsquerdo, tipoDireito) -> {
            if (tipoDireito != tipoEsquerdo) {
                throw new ErroSemantico(
                        "Só se pode realizar operação " + nomeOperacao + " com valores do mesmo tipo!",
                        expressaoBinaria.getDireita().getToken()
                );
            }
            return operacaoEspecifica.apply(tipoDireito);
        });
    }

    private TipoVariavel visitarExpressaoBinariaNumerica(
            ExpressaoBinaria expressaoBinaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Function<TipoVariavel, TipoVariavel> operacaoEspecifica
    ) {
        return visitarExpressaoBinaria(expressaoBinaria, tabela, nomeOperacao, (tipoEsquerdo, tipoDireito) -> {
            if (tipoEsquerdo != TipoVariavel.INTEIRO && tipoEsquerdo != TipoVariavel.FLUTUANTE
                && tipoDireito != TipoVariavel.INTEIRO && tipoDireito != TipoVariavel.FLUTUANTE) {
                throw new ErroSemantico(nomeOperacao + " só pode ser aplicado entre valores numéricos", expressaoBinaria.getToken());
            }

            if (tipoEsquerdo == TipoVariavel.FLUTUANTE || tipoDireito == TipoVariavel.FLUTUANTE) {
                // Quando é feita uma operação entre int e float, o int é convertido para float e o resultado é float
                if (tipoEsquerdo != TipoVariavel.FLUTUANTE) {
                    gerador.gerar("mtc1    $t0, $f0 # convertendo lado esquerdo para float");
                    gerador.gerar("cvt.s.w $f0, $f0");
                } else if (tipoDireito != TipoVariavel.FLUTUANTE) {
                    gerador.gerar("mtc1    $t1, $f1 # convertendo lado direito para float");
                    gerador.gerar("cvt.s.w $f1, $f1");
                }

                return operacaoEspecifica.apply(TipoVariavel.FLUTUANTE);
            }

            return operacaoEspecifica.apply(tipoEsquerdo);
        });
    }

    private TipoVariavel visitarExpressaoBinariaInt(
            ExpressaoBinaria expressaoBinaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Runnable operacaoEspecifica
    ) {
        return visitarExpressaoBinaria(expressaoBinaria, tabela, nomeOperacao, (tipoEsquerdo, tipoDireito) -> {
            if (tipoEsquerdo != TipoVariavel.INTEIRO || tipoDireito != TipoVariavel.INTEIRO) {
                throw new ErroSemantico(nomeOperacao + " só pode ser aplicado entre inteiros", expressaoBinaria.getToken());
            }
            operacaoEspecifica.run();

            return TipoVariavel.INTEIRO;
        });
    }

    // operacaoEspecifica usa os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e deve colocar o resultado em $t0/$f0
    private TipoVariavel visitarExpressaoBinaria(
            ExpressaoBinaria expressaoBinaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            BiFunction<TipoVariavel, TipoVariavel, TipoVariavel> operacaoEspecifica
    ) {
        gerador.gerar("# " + nomeOperacao);

        TipoVariavel tipoEsquerdo = visitarExpressao(expressaoBinaria.getEsquerda(), tabela);
        TipoVariavel tipoDireito = visitarExpressao(expressaoBinaria.getDireita(), tabela);

        // Lê o segundo valor em $t1
        if (tipoDireito == TipoVariavel.FLUTUANTE) {
            desempilhar(tabela, RegistradoresMIPS32.F1);
        } else {
            desempilhar(tabela, RegistradoresMIPS32.T1);
        }
        // Lê o primeiro valor em $t0
        if (tipoEsquerdo == TipoVariavel.FLUTUANTE) {
            desempilhar(tabela, RegistradoresMIPS32.F0);
        } else {
            desempilhar(tabela, RegistradoresMIPS32.T0);
        }

        // Realiza a operação
        TipoVariavel tipoRetorno = operacaoEspecifica.apply(tipoEsquerdo, tipoDireito);

        // Guarda o resultado da soma no stack
        if (tipoRetorno == TipoVariavel.FLUTUANTE) {
            empilhar(tabela, RegistradoresMIPS32.F0);
        } else {
            empilhar(tabela, RegistradoresMIPS32.T0);
        }

        gerador.gerar("# fim " + nomeOperacao);

        return tipoRetorno;
    }

    // operacaoEspecifica usa o valor em $t0 e deve colocar o resultado em $t0/$f0
    private TipoVariavel visitarExpressaoUnaria(
            ExpressaoUnaria expressaoUnaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Consumer<TipoVariavel> operacaoEspecifica
    ) {
        gerador.gerar("# inicio " + nomeOperacao);

        // Avalia a expressão e empilha o resultado
        TipoVariavel tipoVariavel = visitarExpressao(expressaoUnaria.getExpressao(), tabela);
        if (tipoVariavel == TipoVariavel.FLUTUANTE) {
            desempilhar(tabela, RegistradoresMIPS32.F0);
        } else {
            desempilhar(tabela, RegistradoresMIPS32.T0);
        }

        operacaoEspecifica.accept(tipoVariavel);
        // Empilha o resultado
        if (tipoVariavel == TipoVariavel.FLUTUANTE) {
            empilhar(tabela, RegistradoresMIPS32.F0);
        } else {
            empilhar(tabela, RegistradoresMIPS32.T0);
        }

        gerador.gerar("# fim " + nomeOperacao);

        return tipoVariavel;
    }
}
