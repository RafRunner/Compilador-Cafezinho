package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.ComandoComExpressao;
import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.compilador.tabeladesimbolos.*;

import java.util.List;

// BlocoDeclaracoes, BlocoPrograma, Declaracao, Comando ComandoBloco, ComandoComExpressao, ComandoEnquanto, ComandoLeia,
// ComandoNovaLinha, ComandoRetorno, ComandoSe, DeclaracaoDeVariavel, DeclaracaoFuncao, DeclaracaoFuncaoEVariaveis,
// DeclaracaoVariaveisEmBloco, Expressao, ExpressaoAtribuicao, ExpressaoBinaria, ExpressaoCaractereLiteral,
// ExpressaoChamadaFuncao, ExpressaoDiferente, ExpressaoDivisao, ExpressaoE, ExpressaoEntreparenteses, ExpressaoIdentificado,
// ExpressaoIgual, ExpressaoInteiroLiteral, ExpressaoMaior, ExpressaoLiteral, ExpressaoMaiorIgual, ExpressaoMais, ExpressaoMenor,
// ExpressaoMenorIgual, ExpressaoMenos, Expressaonegativo, ExpressaoNegacao, ExpressaoOu, ExpressaoOu, ExpressaoResto,
// ExpressaoStringLiteral, ExpressaoTernaria, ExpressaoUnaria, ExpressaoVezes, ParametroFuncao, TipoVariavelNo, Varivel

public class VisitadorDeNosMIPS32 implements VisitadorDeNos {

    private final TabelaDeSimbolos tabelaGlobal;
    private final GeradorDeCodigo gerador;
    private final Programa programa;

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
            gerador.gerar("addiu $sp, $sp, -4");
            gerador.gerar("sw    $ra, 0($sp)");

            int offset = -4; // Começa após salvar $ra
            int offsetInicial = offset;

            for (ParametroFuncao param : parametros) {
                offset -= getEspacoMemoriaParametro(param);
                tabelaFuncao.adicionaSimbolo(new SimboloParametroFuncao(param, offsetInicial - offset));
            }

            tabelaFuncao.setOffset(offset);

            // Gerar código do corpo da função
            visitarEscopo(funcao.getCorpo(), tabelaFuncao);

            // Epílogo da função
            gerador.gerar("lw    $ra, 0($sp)");
            gerador.gerar("addiu $sp, $sp, 4");
            gerador.gerar("jr    $ra");
        }
    }

    @Override
    public void visitarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.gerar(".global main");
        gerador.gerar("main:");

        visitarEscopo(blocoPrograma.getBlocoDeclaracoes(), tabelaGlobal);

        // Finalizar o programa
        gerador.gerar("li $v0, 10"); // Syscall para saída
        gerador.gerar("syscall");    // Executar syscall
    }

    // Marca um novo escopo, com nova tabela
    @Override
    public void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaPai) {
        TabelaDeSimbolos tabelaDoEscopo = tabelaPai.criaBlocoInterno();

        int offsetInicial = tabelaPai.getOffset();

        for (Declaracao declaracao : blocoDeclaracoes.getDeclaracoes()) {
            if (declaracao instanceof DeclaracaoVariavelEmBloco) {
                visitarDeclaracaoDeVariaveisEmBloco((DeclaracaoVariavelEmBloco) declaracao, tabelaDoEscopo);
            }
            else if (declaracao instanceof ComandoComExpressao) {
                Expressao expressao = ((ComandoComExpressao) declaracao).getExpressao();

                if (expressao instanceof ExpressaoIdentificador) {
                    visitaIdentificador((ExpressaoIdentificador) expressao, tabelaDoEscopo);
                }
            }
        }

        // Código para reajustar o stack pointer no final do escopo
        gerador.gerar("addi  $sp, $sp, " + (offsetInicial - tabelaDoEscopo.getOffset()));
    }

    // Aqui temos somente variáveis locais
    @Override
    public void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco) {
        int offset = tabelaBloco.getOffset();
        int offsetInicial = offset;

        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                offset -= getEspacoMemoriaVariavelLocal(var);

                tabelaBloco.adicionaSimbolo(new SimboloVariavelLocal(var, offsetInicial - offset));

                if (var.isVetor()) {
                    // Aloca o vetor e guarda o endereço em $t0
                    alocaVetor(var, RegistradoresMIPS32.T0);
                }
            }
        }

        // Reserva espaço para todas as variáveis locais
        gerador.gerar("addi  $sp, $sp, " + (offset - offsetInicial));

        tabelaBloco.setOffset(offset);
    }

    @Override
    public void visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabelaDeSimbolos) {
        String nomeVariavel = identificador.getIdentificador();

        // Verifique se a variável existe
        Simbolo simbolo = tabelaDeSimbolos.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            this.programa.reportaErroSemantico("Variável não declarada: " + nomeVariavel, identificador.getToken());
        }

        // Checar se é uma variável local ou global
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();

            if (variavel.isVetor()) {
            } else {
                leVariavelGlobal(variavelGlobal, RegistradoresMIPS32.T0);
            }
        } else {
            SimboloVariavelLocal variavelLocal = (SimboloVariavelLocal) simbolo;
            Variavel variavel = variavelLocal.getNoSintatico();
            int offset = variavelLocal.getOffset();

            if (variavel.isVetor()) {

            } else {

            }
        }
    }

    private int getEspacoMemoria(TipoVariavel tipo) {
        if (tipo == TipoVariavel.INTEIRO) {
            return 4;   // int tem 4 bytes -> 32 bits
        } else {
            return 1;   // um caractere é um byte
        }
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

    private void alocaVetor(Variavel variavel, RegistradoresMIPS32 registrador) {
        int tamanho = getEspacoMemoria(variavel.getTipo().getTipo()) * variavel.getTamanhoVetor();
        gerador.gerar("li $v0, 9");                                  // Syscall número 9 para alocar memória
        gerador.gerar("li $a0, " + tamanho);                         // Carregar tamanho do array em $a0
        gerador.gerar("syscall");                                    // Executar syscall
        gerador.gerar("move " + registrador.getNome() + ", $v0");    // Endereço base do array alocado está agora no registrador
        gerador.gerar("sw   " + registrador.getNome() + ", 0($sp)"); // Guarda o endereço do vetor no stack
    }

    private void leVariavelGlobal(Simbolo simbolo, RegistradoresMIPS32 registrador) {
        gerador.gerar("la $t1, " + simbolo.getNome());            // carrega endereço da variável em $t1
        gerador.gerar("lw " + registrador.getNome() + " 0($t1)"); // carrega o valor no endereço no registrador passado
    }
}
