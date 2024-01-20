package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.tabeladesimbolos.*;
import src.raiz.erros.BugCompilador;
import src.raiz.erros.ErroSemantico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// BlocoDeclaracoes, BlocoPrograma, Declaracao, Comando, ComandoBloco, ComandoComExpressao, ComandoEnquanto, ComandoLeia,
// ComandoNovaLinha, ComandoRetorno, ComandoSe, DeclaracaoDeVariavel, DeclaracaoFuncao, DeclaracaoFuncaoEVariaveis,
// DeclaracaoVariaveisEmBloco, Expressao, ExpressaoAtribuicao, ExpressaoBinaria, ExpressaoCaractereLiteral, ComandoEscreva
// ExpressaoChamadaFuncao, ExpressaoDiferente, ExpressaoDivisao, ExpressaoE, ExpressaoEntreparenteses, ExpressaoIdentificado,
// ExpressaoIgual, ExpressaoInteiroLiteral, ExpressaoMaior, ExpressaoLiteral, ExpressaoMaiorIgual, ExpressaoMais, ExpressaoMenor,
// ExpressaoMenorIgual, ExpressaoMenos, Expressaonegativo, ExpressaoNegacao, ExpressaoOu, ExpressaoResto,
// ExpressaoStringLiteral, ExpressaoTernaria, ExpressaoUnaria, ExpressaoVezes, ParametroFuncao, TipoVariavelNo, Varivel

public class VisitadorDeNosMIPS32 implements VisitadorDeNos {

    private final TabelaDeSimbolos tabelaGlobal;
    private final GeradorDeCodigo gerador;
    private final Programa programa;
    private final Map<String, String> stringsParaLabels = new HashMap<>();

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
    public void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node) {
        // Processar declarações de variáveis globais
        gerador.setModoAtual(ModoGerador.VARIAVEIS_GLOBAIS);

        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                tabelaGlobal.adicionaSimbolo(new SimboloVariavelGlobal(var));

                String nomeVariavel = var.getNome();

                // Gerar código para variável global
                gerador.gerar(nomeVariavel + ": .space " + getEspacoMemoriaVariavelGlobal(var));
            }
        }

        gerador.setModoAtual(ModoGerador.FUNCAO);

        for (DeclaracaoFuncao funcao : node.getDeclaracoesDeFuncoes()) {
            String nomeFuncao = funcao.getNome();
            List<ParametroFuncao> parametros = funcao.getParametros();

            // Tabela que contêm as variáveis locais da função, incluindo os parâmetros de chamada
            TabelaDeSimbolos tabelaFuncao = tabelaGlobal.criaBlocoInterno();

            tabelaGlobal.adicionaSimbolo(new SimboloFuncao(funcao, tabelaFuncao));

            // Gerar etiqueta (label) da função
            gerador.gerar(nomeFuncao + ":");

            // Prólogo da função
            gerador.gerar("addiu $sp, $sp, -4 # prólogo função");
            gerador.gerar("sw    $ra, 0($sp)");

            int offset = 4; // Começa após salvar $ra
            for (ParametroFuncao param : parametros) {
                tabelaFuncao.adicionaSimbolo(new SimboloParametroFuncao(param, offset));

                tabelaFuncao.setOffset(offset);
                offset += getEspacoMemoriaParametro(param);
            }

            // Gerar código do corpo da função
            visitarEscopo(funcao.getCorpo(), tabelaFuncao);

            // Epílogo da função
            gerador.gerar("lw    $ra, 0($sp) # epílogo função");
            gerador.gerar("addiu $sp, $sp, 4");
            gerador.gerar("jr    $ra         #fim função " + funcao.getNome() + "\n");
        }
    }

    @Override
    public void visitarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.gerar(".globl main");
        gerador.gerar("main:");

        visitarEscopo(blocoPrograma.getBlocoDeclaracoes(), tabelaGlobal);

        // Finalizar o programa
        gerador.gerar("li    $v0, 10 # finalizando programa"); // Syscall para saída
        gerador.gerar("syscall       # fim main");    // Executar syscall
    }

    // Marca um novo escopo, com nova tabela
    @Override
    public void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaPai) {
        TabelaDeSimbolos tabelaDoEscopo = tabelaPai.criaBlocoInterno();

        int offsetInicial = tabelaPai.getOffset();

        for (Declaracao declaracao : blocoDeclaracoes.getDeclaracoes()) {
            if (declaracao instanceof ComandoBloco) {
                visitarEscopo(((ComandoBloco) declaracao).getDeclaracoes(), tabelaPai);
            }
            else if (declaracao instanceof DeclaracaoVariavelEmBloco) {
                visitarDeclaracaoDeVariaveisEmBloco((DeclaracaoVariavelEmBloco) declaracao, tabelaDoEscopo);
            }
            else if (declaracao instanceof ComandoEscreva) {
                visitarComandoEscreva((ComandoEscreva) declaracao, tabelaDoEscopo);
            }
            else if (declaracao instanceof ComandoLeia) {
                visitarComandoLeia((ComandoLeia) declaracao, tabelaDoEscopo);
            }
            else if (declaracao instanceof ComandoNovalinha) {
                visitarComandoNovalinha((ComandoNovalinha) declaracao);
            }
            else if (declaracao instanceof ComandoComExpressao) {
                Expressao expressao = ((ComandoComExpressao) declaracao).getExpressao();

                visitarExpressao(expressao, tabelaDoEscopo);
                // Limpando efeito colateral
                desempilharEmS0(tabelaDoEscopo);
            }
        }

        // Código para reajustar o stack pointer no final do escopo
        gerador.gerar("addi  $sp, $sp, " + (tabelaDoEscopo.getOffset() - offsetInicial) + " # reajusta stack bloco");
    }

    // Após executar uma expressão, seu resultado estará no topo do stack
    @Override
    public TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo) {
        if (expressao instanceof ExpressaoEntreParenteses) {
            ExpressaoEntreParenteses expressaoEntre = (ExpressaoEntreParenteses) expressao;
            visitarExpressao(expressaoEntre.getExpressao(), tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoIdentificador) {
            return visitaIdentificador((ExpressaoIdentificador) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoInteiroLiteral) {
            return visitarExpressaoInteiroLiteral((ExpressaoInteiroLiteral) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoCaractereLiteral) {
            return visitarExpressaoCaractereLiteral((ExpressaoCaractereLiteral) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoStringLiteral) {
            return visitarExpressaoStringLiteral((ExpressaoStringLiteral) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoAtribuicao) {
            return visitarExpressaoAtribuicao((ExpressaoAtribuicao) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMais) {
            return visitarExpressaoSoma((ExpressaoMais) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMenos) {
            return visitarExpressaoSubtracao((ExpressaoMenos) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoVezes) {
            return visitarExpressaoVezes((ExpressaoVezes) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoDivisao) {
            return visitarExpressaoDivisao((ExpressaoDivisao) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoE) {
            return visitarExpressaoE((ExpressaoE) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoOu) {
            return visitarExpressaoOu((ExpressaoOu) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoIgual) {
            return visitarExpressaoIgual((ExpressaoIgual) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoDiferente) {
            return visitarExpressaoDiferente((ExpressaoDiferente) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMaior) {
            return visitarExpressaoMaior((ExpressaoMaior) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMaiorIgual) {
            return visitarExpressaoMaiorIgual((ExpressaoMaiorIgual) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMenor) {
            return visitarExpressaoMenor((ExpressaoMenor) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoMenorIgual) {
            return visitarExpressaoMenorIgual((ExpressaoMenorIgual) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoResto) {
            return visitarExpressaoResto((ExpressaoResto) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoTernaria) {
            return visitarExpressaoTernaria((ExpressaoTernaria) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoNegativo) {
            return visitarExpressaoNegativo((ExpressaoNegativo) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoNegacao) {
            return visitarExpressaoNegacao((ExpressaoNegacao) expressao, tabelaDoEscopo);
        }
        if (expressao instanceof ExpressaoLeia) {
            return visitarExpressaoLeia((ExpressaoLeia) expressao, tabelaDoEscopo);
        }

        // TODO lançar erro aqui, expressão não identificada
        return TipoVariavel.INTEIRO;
    }

    // Aqui temos somente variáveis locais
    @Override
    public void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco) {
        int offset = tabelaBloco.getOffset();

        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                tabelaBloco.adicionaSimbolo(new SimboloVariavelLocal(var, offset));

                tabelaBloco.setOffset(offset);
                if (var.isVetor()) {
                    // Aloca o vetor e guarda o endereço no stack
                    alocaVetor(var, offset, tabelaBloco);
                }
                offset += 4;
                gerador.gerar("addi  $sp, $sp, -4 # reservando espaço próxima variável " + var.getNome());
            }
        }

        gerador.gerar("addi  $sp, $sp, 4 # corrige ponteiro $sp");
    }

    @Override
    public TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        String nomeVariavel = identificador.getIdentificador();

        // Verifique se a variável existe
        Simbolo<?> simbolo = tabela.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nomeVariavel, identificador.getToken());
        }

        if (simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO) {
            throw new ErroSemantico(nomeVariavel + " é uma função, deve ser invocada", identificador.getToken());
        }

        // Checar se é uma variável local ou global
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();
            TipoVariavel tipoVariavel = variavel.getTipo().getTipo();

            if (variavel.isVetor()) {
                if (identificador.getIndex() != null) {
                    visitarExpressaoIndex(identificador, tabela);
                    leVariavelGlobalVetorIndexado(variavelGlobal, tabela);
                } else {
                    leVariavelGlobal(variavelGlobal.getNome(), tipoVariavel, tabela);
                }
            } else {
                if (identificador.getIndex() != null) {
                    throw new ErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                }
                leVariavelGlobal(variavel.getNome(), tipoVariavel, tabela);
            }

            return tipoVariavel;
        } else {
            VariavelNoStack variavelLocal;
            if (simbolo.getTipoSimbolo() == TipoSimbolo.PARAMETRO_FUNCAO) {
                variavelLocal = ((SimboloParametroFuncao) simbolo).getVariavelNoStack();
            } else {
                variavelLocal = ((SimboloVariavelLocal) simbolo).getVariavelNoStack();
            }

            if (variavelLocal.isVetor()) {
                if (identificador.getIndex() != null) {
                    visitarExpressaoIndex(identificador, tabela);
                    leVariavelLocalVetorIndexado(variavelLocal, tabela);
                } else {
                    leVariavelLocal(variavelLocal.getNome(), variavelLocal.getOffset(), tabela);
                }
            } else {
                if (identificador.getIndex() != null) {
                    throw new ErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                }
                leVariavelLocal(variavelLocal.getNome(), variavelLocal.getOffset(), tabela);
            }

            return variavelLocal.getTipoVariavel();
        }
    }

    @Override
    public TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela) {
        int valor = expressao.getConteudo();
        gerador.gerar("li    $s0, " + valor + " # empilhando inteiro literal " + valor);  // Carregar valor no registrador $s0
        empilharS0(tabela); // Empilhar o valor no stack

        return TipoVariavel.INTEIRO;
    }

    @Override
    public TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela) {
        char valor = expressao.getConteudo();
        gerador.gerar("li    $s0, " + (int) valor + " # empilhando caractere literal " + valor); // Carregar valor ASCII no registrador $ts0
        empilharS0(tabela); // Empilhar o valor no stack

        return TipoVariavel.CARACTERE;
    }

    @Override
    public TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela) {
        String valor = expressao.getConteudo();
        String label = gerarLabelUnico(); // Função auxiliar para gerar um label único para a string

        if (!stringsParaLabels.containsKey(valor)) {
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
        gerador.gerar("li    $a0, 10"); // ACII do \n
        gerador.gerar("syscall       # fim novaLinha");
    }

    @Override
    public TipoVariavel visitarExpressaoSoma(ExpressaoMais expressaoMais, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMais, tabela, "soma", () -> {
            gerador.gerar("add   $t0, $t0, $t1 # soma $t0 e $s1, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoSubtracao(ExpressaoMenos expressaoMenos, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMenos, tabela, "subtração", () -> {
            gerador.gerar("sub   $t0, $t0, $t1 # subtrai $t1 de $t0, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoVezes(ExpressaoVezes expressaoVezes, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoVezes, tabela, "multiplicação", () -> {
            gerador.gerar("mul   $t0, $t0, $t1 # multiplica $t0 por $t1, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoDivisao(ExpressaoDivisao expressaoDivisao, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoDivisao, tabela, "divisão", () -> {
            gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
            gerador.gerar("mflo  $t0      # move o resultado da divisão para $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoE(ExpressaoE expressaoE, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoE, tabela, "e", () -> {
            gerador.gerar("and   $t0, $t0, $t1 # operação lógica E entre $t0 e $t1, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoOu(ExpressaoOu expressaoOu, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoOu, tabela, "ou", () -> {
            gerador.gerar("or    $t0, $t0, $t1 # operação lógica Ou entre $t0 e $t1, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoIgual(ExpressaoIgual expressaoIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoIgual, tabela, "igual", () -> {
            gerador.gerar("sub   $t0, $t0, $t1 # subtrai $t1 de $t0");
            gerador.gerar("sltiu $t0, $t0, 1   # define $t0 como 1 se $t0 era 0 (iguais), ou 0 caso contrário");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoDiferente(ExpressaoDiferente expressaoDiferente, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoDiferente, tabela, "igual", () -> {
            gerador.gerar("sub   $t0, $t0, $t1   # subtrai $t1 de $t0");
            gerador.gerar("sltu  $t0, $zero, $t0 # define $t0 como 1 se são diferentes, 0 se iguais");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMaior(ExpressaoMaior expressaoMaior, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMaior, tabela, "maior", () -> {
            gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMaiorIgual(ExpressaoMaiorIgual expressaoMaiorIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMaiorIgual, tabela, "maior igual", () -> {
            gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1");
            gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'maior ou igual'");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMenor(ExpressaoMenor expressaoMenor, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMenor, tabela, "menor", () -> {
            gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1, resultado em $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoMenorIgual(ExpressaoMenorIgual expressaoMenorIgual, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoMenorIgual, tabela, "menor igual", () -> {
            gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0");
            gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'menor ou igual'");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoResto(ExpressaoResto expressaoResto, TabelaDeSimbolos tabela) {
        return visitarExpressaoBinaria(expressaoResto, tabela, "resto", () -> {
            gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
            gerador.gerar("mfhi  $t0      # move o resto da divisão para $t0");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela) {
        String nomeVariavel = expressaoAtribuicao.getIdentificador().getIdentificador();
        Simbolo<?> simbolo = tabela.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nomeVariavel, expressaoAtribuicao.getToken());
        }

        Expressao ladoDireito = expressaoAtribuicao.getExpressaoLadoDireito();
        TipoVariavel tipoDireito = visitarExpressao(ladoDireito, tabela);

        if (simbolo.getTipoSimbolo() == TipoSimbolo.FUNCAO) {
            throw new ErroSemantico(nomeVariavel + " é uma função, não pode ter valor atribuído", expressaoAtribuicao.getToken());
        }

        // Não desempilhamos aqui pois desejamos que o valor continue no stack

        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            // Atribuir valor a uma variável global
            Variavel variavel = ((SimboloVariavelGlobal) simbolo).getNoSintatico();

            if (tipoDireito != variavel.getTipo().getTipo()) {
                throw new ErroSemantico(
                        "A variável " + variavel.getNome() + " é do tipo " + variavel.getTipo().getTipo() + " e não pode receber valor do tipo " + tipoDireito,
                        expressaoAtribuicao.getToken()
                );
            }

            if (variavel.isVetor() && expressaoAtribuicao.getIdentificador().getIndex() != null) {
                int tamanhoElemento = getEspacoMemoria(variavel.getTipo().getTipo());
                visitarExpressaoIndex(expressaoAtribuicao.getIdentificador(), tabela);
                desempilharEmS0(tabela); // Desempilha o index

                // Carregar o endereço base do vetor global
                gerador.gerar("la    $t0, " + variavel.getNome() + " # carrega endereço do vetor global em $t0 " + variavel.getNome());
                gerador.gerar("li    $t1, " + tamanhoElemento);
                gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
                gerador.gerar("add   $t0, $t1, $t0 # soma o endereço com o index * tamanho");

                gerador.gerar("lw    $t3, 0($sp) # lê o valor a ser armazenado");

                if (variavel.getTipo().getTipo() == TipoVariavel.CARACTERE) {
                    gerador.gerar("sb    $t3, 0($t0) # armazena byte");
                } else {
                    gerador.gerar("sw    $t3, 0($t0) # armazena int");
                }
            } else {
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável global " + variavel.getNome()); // Carregar valor do topo do stack
                gerador.gerar("la    $t1, " + nomeVariavel); // Carregar endereço da variável global
                gerador.gerar("sw    $t0, 0($t1)"); // Armazenar valor na variável global
            }

            return variavel.getTipo().getTipo();
        } else {
            // Atribuir valor a uma variável local
            VariavelNoStack variavelLocal;
            if (simbolo.getTipoSimbolo() == TipoSimbolo.PARAMETRO_FUNCAO) {
                variavelLocal = ((SimboloParametroFuncao) simbolo).getVariavelNoStack();
            } else {
                variavelLocal = ((SimboloVariavelLocal) simbolo).getVariavelNoStack();
            }
            int offset = tabela.getOffset() - variavelLocal.getOffset();

            if (tipoDireito != variavelLocal.getTipoVariavel()) {
                throw new ErroSemantico(
                        "A variável " + variavelLocal.getNome() + " é do tipo " + variavelLocal.getTipoVariavel() + " e não pode receber valor do tipo " + tipoDireito,
                        expressaoAtribuicao.getToken()
                );
            }

            if (variavelLocal.isVetor() && expressaoAtribuicao.getIdentificador().getIndex() != null) {
                int tamanhoElemento = getEspacoMemoria(variavelLocal.getTipoVariavel());
                visitarExpressaoIndex(expressaoAtribuicao.getIdentificador(), tabela);
                desempilharEmS0(tabela);  // Desempilha o index

                gerador.gerar("lw    $t0, " + offset + "($sp) # carrega endereço do vetor em $t0 " + variavelLocal.getNome());
                gerador.gerar("li    $t1, " + tamanhoElemento);
                gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
                gerador.gerar("add   $t0, $t1, $t0 # soma o endereço com o index * tamanho");

                gerador.gerar("lw    $t3, 0($sp) # lê o valor a ser armazenado");

                if (variavelLocal.getTipoVariavel() == TipoVariavel.CARACTERE) {
                    gerador.gerar("sb    $t3, 0($t0) # armazena byte");
                } else {
                    gerador.gerar("sw    $t3, 0($t0) # armazena int");
                }

            } else {
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável local " + variavelLocal.getNome()); // Carregar valor do topo do stack
                gerador.gerar("sw    $t0, " + offset + "($sp)"); // Armazenar valor na variável local
            }

            return variavelLocal.getTipoVariavel();
        }
    }

    @Override
    public TipoVariavel visitarExpressaoTernaria(ExpressaoTernaria expressaoTernaria, TabelaDeSimbolos tabela) {
        gerador.gerar("# inicio expressão ternária");

        // Avaliar condição
        visitarExpressao(expressaoTernaria.getCondicao(), tabela);
        desempilharEmS0(tabela); // Carrega o resultado da condição em $t0

        String labelFalso = "labelTernarioFalso_" + gerarLabelUnico();
        String labelFim = "labelTernarioFim_" + gerarLabelUnico();

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
        return visitarExpressaoUnaria(expressaoNegacao, tabela, "negação", () -> {
            gerador.gerar("seq   $t0, $t0, $zero # inverte o valor booleano, 1 se $t0 é 0, 0 caso contrário");
        });
    }

    @Override
    public TipoVariavel visitarExpressaoNegativo(ExpressaoNegativo expressaoNegativo, TabelaDeSimbolos tabela) {
        return visitarExpressaoUnaria(expressaoNegativo, tabela, "negativo", () -> {
            gerador.gerar("sub   $t0, $zero, $t0 # inverte o sinal do número");
        });
    }

    @Override
    public void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = visitarExpressao(comandoEscreva.getExpressao(), tabela);

        gerador.gerar("# inicio escreva");

        switch (tipo) {
            case INTEIRO:
                gerador.gerar("li    $v0, 1");  // Syscall para imprimir inteiro
                break;
            case CARACTERE:
                gerador.gerar("li    $v0, 11"); // Syscall para imprimir char
                break;
            case STRING:
                gerador.gerar("li    $v0, 4");  // Syscall para imprimir string
                break;
        }

        gerador.gerar("lw    $a0, 0($sp)"); // Carregar o inteiro do topo do stack
        gerador.gerar("syscall # fim escreva");

        desempilharEmS0(tabela);
    }

    @Override
    public void visitarComandoLeia(ComandoLeia comandoLeia, TabelaDeSimbolos tabela) {
        gerador.gerar("# inicio comando leia");

        ExpressaoIdentificador identificador = comandoLeia.getExpressaoIdentificador();
        Simbolo<?> simbolo = tabela.getSimbolo(identificador.getIdentificador());

        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + identificador.getIdentificador(), comandoLeia.getToken());
        }

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

    // Expressão que lê o que foi digitado e empilha
    private TipoVariavel visitarExpressaoLeia(ExpressaoLeia expressaoLeia, TabelaDeSimbolos tabela) {
        if (expressaoLeia.getTipoVariavel() == TipoVariavel.INTEIRO) {
            gerador.gerar("li    $v0, 5 # syscall para ler inteiro");
            gerador.gerar("syscall");
        } else if (expressaoLeia.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("li    $v0, 12 # syscall para ler caractere");
            gerador.gerar("syscall");
        } else {
            throw new ErroSemantico("Tipo não suportado para leitura", expressaoLeia.getToken());
        }

        empilhar(tabela, RegistradoresMIPS32.V0);

        gerador.gerar("# fim comando leia");

        return expressaoLeia.getTipoVariavel();
    }

    private int getEspacoMemoria(TipoVariavel tipo) {
        switch (tipo) {
            case INTEIRO:
            case STRING:
                return 4; // int tem 4 bytes -> 32 bits. Uma string é um ponteiro
            case CARACTERE:
                return 1; // um caractere é um byte
        }

        throw new BugCompilador("Tipo " + tipo + " não identificado");
    }

    private int getEspacoMemoriaVariavelGlobal(Variavel variavel) {
        int tamanhoBase = getEspacoMemoria(variavel.getTipo().getTipo());

        if (variavel.isVetor()) {
            return tamanhoBase * variavel.getTamanhoVetor(); // É vetor
        } else {
            return tamanhoBase; // não é vetor
        }
    }

    private int getEspacoMemoriaVariavelLocal(Variavel variavel) {
        if (variavel.isVetor()) {
            return 4; // Se for um vetor, é um ponteiro de 32 bits
        } else {
            return getEspacoMemoria(variavel.getTipo().getTipo());
        }
    }

    private int getEspacoMemoriaParametro(ParametroFuncao parametro) {
        if (parametro.isVetor()) {
            return 4; // Se for um vetor, é um ponteiro de 32 bits
        } else {
            return getEspacoMemoria(parametro.getTipo().getTipo());
        }
    }

    // Assumimos que foi criado espaço no stack para armazenar o endereço do vetor
    private void alocaVetor(Variavel variavel, int offset, TabelaDeSimbolos tabela) {
        int tamanho = getEspacoMemoria(variavel.getTipo().getTipo()) * variavel.getTamanhoVetor();

        gerador.gerar("li    $v0, 9 # aloca vetor " + variavel.getNome()); // Syscall número 9 para alocar memória
        gerador.gerar("li    $a0, " + tamanho); // Carregar tamanho do array em $a0
        gerador.gerar("syscall");               // Executar syscall
        gerador.gerar("move  $s0, $v0");        // Endereço base do array alocado está agora no registrador $s0
        // Guarda o endereço do vetor no stack
        gerador.gerar("sw    $s0, " + (tabela.getOffset() - offset) + "($sp) # fim aloca vetor " + variavel.getNome());
    }

    private void leVariavelGlobal(String nome, TipoVariavel tipo, TabelaDeSimbolos tabela) {
        gerador.gerar("la    $t1, " + nome + " # lendo variável global " + nome); // carrega endereço da variável em $t1
        if (tipo == TipoVariavel.CARACTERE) {
            gerador.gerar("lb    $s0, 0($t1)"); // carrega o valor no endereço no registrador passado
        } else {
            gerador.gerar("lw    $s0, 0($t1)"); // carrega o valor no endereço no registrador passado
        }
        empilharS0(tabela);
        gerador.gerar("# fim lendo variável global " + nome);
    }

    private void leVariavelGlobalVetorIndexado(SimboloVariavelGlobal variavelGlobal, TabelaDeSimbolos tabela) {
        int tamanhoElemento = getEspacoMemoria(variavelGlobal.getTipoVariavel());

        // Carregar o endereço base do vetor global
        gerador.gerar("la    $t0, " + variavelGlobal.getNome() + " # lendo vetor global indexado " + variavelGlobal.getNome());

        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(tabela);

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor
        if (variavelGlobal.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("lb    $s0, 0($t1)");
        } else {
            gerador.gerar("lw    $s0, 0($t1)");
        }

        // Empilhar o valor do elemento no stack
        empilharS0(tabela);

        gerador.gerar("# fim lendo vetor global indexado " + variavelGlobal.getNome());
    }

    private void leVariavelLocal(String nome, int offset, TabelaDeSimbolos tabela) {
        gerador.gerar("lw    $s0, " + (tabela.getOffset() - offset) + "($sp) # lendo variável local " + nome); // Carrega o valor da variável local em $s0
        empilharS0(tabela);
        gerador.gerar("# fim lendo variável local " + nome);
    }

    private void leVariavelLocalVetorIndexado(VariavelNoStack variavelLocal, TabelaDeSimbolos tabela) {
        int offset = variavelLocal.getOffset();
        int tamanhoElemento = getEspacoMemoria(variavelLocal.getTipoVariavel());
        int tamanhoStackAtual = tabela.getOffset();

        gerador.gerar("# lendo vetor local indexado " + variavelLocal.getNome());

        gerador.gerar("lw    $t0, " + (tamanhoStackAtual - offset) + "($sp) # lendo endereço do vetor");
        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(tabela);
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t0, $t0, $t1");

        if (variavelLocal.getTipoVariavel() == TipoVariavel.CARACTERE) {
            gerador.gerar("lb    $s0, 0($t0) # lê byte");
        } else {
            gerador.gerar("lw    $s0, 0($t0) # lê inteiro");
        }

        empilharS0(tabela);

        gerador.gerar("# fim lendo vetor local indexado " + variavelLocal.getNome());
    }

    private void empilharS0(TabelaDeSimbolos tabela) {
        empilhar(tabela, RegistradoresMIPS32.S0);
    }

    private void empilhar(TabelaDeSimbolos tabela, RegistradoresMIPS32 registrador) {
        gerador.gerar("addiu $sp, $sp, -4");                          // Aloca espaço no stack
        gerador.gerar("sw    " + registrador.getNome() + ", 0($sp)");  // Guarda a variável (em $s0) no stack

        tabela.setOffset(tabela.getOffset() + 4);
    }

    private void desempilharEmS0(TabelaDeSimbolos tabela) {
        desempilhar(tabela, RegistradoresMIPS32.S0);
    }

    private void desempilhar(TabelaDeSimbolos tabela, RegistradoresMIPS32 registrador) {
        gerador.gerar("lw    " + registrador.getNome() + ", 0($sp)"); // Guarda o topo do stack no registrador
        gerador.gerar("addiu $sp, $sp, 4");                           // Desaloca espaço no stack

        tabela.setOffset(tabela.getOffset() - 4);
    }

    private String gerarLabelUnico() {
        int tamanho = 20;
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < tamanho; i++) {
            // Valor ASCII para letras maiúsculas (65 até 90)
            int valorAscii = random.nextInt(26) + 65;
            char letraAleatoria = (char) valorAscii;
            sb.append(letraAleatoria);
        }

        return sb.toString();
    }

    private void visitarExpressaoIndex(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        TipoVariavel tipoVariavel = visitarExpressao(identificador.getIndex(), tabela);
        if (tipoVariavel != TipoVariavel.INTEIRO) {
            throw new ErroSemantico("Expressão de index deve ser um inteiro!", identificador.getToken());
        }
    }

    // operacaoEspecifica usa os valores de um mesmo tipo em $t0 (esquerdo) e $t1 (direito) e deve colocar o resultado em $t0
    private TipoVariavel visitarExpressaoBinaria(
            ExpressaoBinaria expressaoBinaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Runnable operacaoEspecifica
    ) {
        gerador.gerar("# " + nomeOperacao);

        TipoVariavel ladoEsquerdo = visitarExpressao(expressaoBinaria.getEsquerda(), tabela);
        TipoVariavel ladoDireito = visitarExpressao(expressaoBinaria.getDireita(), tabela);

        if (ladoDireito != ladoEsquerdo) {
            throw new ErroSemantico(
                    "Só se pode realizar " + nomeOperacao + " com valores do mesmo tipo!",
                    expressaoBinaria.getDireita().getToken()
            );
        }

        desempilhar(tabela, RegistradoresMIPS32.T1); // Lê o segundo valor em $t1
        desempilhar(tabela, RegistradoresMIPS32.T0); // Lê o primeiro valor em $t0

        // Realiza a operação
        operacaoEspecifica.run();

        // Guarda o resultado da soma no stack
        empilhar(tabela, RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);

        return TipoVariavel.INTEIRO;
    }

    // operacaoEspecifica usa o valor em $t0 e deve colocar o resultado em $t0
    private TipoVariavel visitarExpressaoUnaria(
            ExpressaoUnaria expressaoUnaria,
            TabelaDeSimbolos tabela,
            String nomeOperacao,
            Runnable operacaoEspecifica
    ) {
        gerador.gerar("# inicio " + nomeOperacao);

        // Avalia a expressão e empilha o resultado
        TipoVariavel tipoVariavel = visitarExpressao(expressaoUnaria.getExpressao(), tabela);
        if (tipoVariavel != TipoVariavel.INTEIRO) {
            throw new ErroSemantico("Não se pode aplicar " + nomeOperacao + " a valores não inteiros", expressaoUnaria.getToken());
        }
        desempilhar(tabela, RegistradoresMIPS32.T0);

        operacaoEspecifica.run();
        // Empilha o resultado
        empilhar(tabela, RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);

        return TipoVariavel.INTEIRO;
    }
}
