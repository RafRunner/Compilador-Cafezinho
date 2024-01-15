package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.ComandoComExpressao;
import src.raiz.ast.expressoes.ExpressaoCaractereLiteral;
import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.ast.expressoes.ExpressaoInteiroLiteral;
import src.raiz.ast.expressoes.ExpressaoStringLiteral;
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

        gerador.gerar("");

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
            gerador.gerar("# prólogo função:");
            gerador.gerar("addiu $sp, $sp, -4");
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
            gerador.gerar("# epílogo função:");
            gerador.gerar("lw    $ra, 0($sp)");
            gerador.gerar("addiu $sp, $sp, 4");
            gerador.gerar("jr    $ra");
            gerador.gerar("# fim função " + funcao.getNome() + "\n");
        }
    }

    @Override
    public void visitarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.gerar(".global main");
        gerador.gerar("main:");

        visitarEscopo(blocoPrograma.getBlocoDeclaracoes(), tabelaGlobal);

        // Finalizar o programa
        gerador.gerar("# finalizando programa");
        gerador.gerar("li $v0, 10"); // Syscall para saída
        gerador.gerar("syscall");    // Executar syscall
        gerador.gerar("# fim main\n");
    }

    // Marca um novo escopo, com nova tabela
    @Override
    public void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaPai) {
        TabelaDeSimbolos tabelaDoEscopo = tabelaPai.criaBlocoInterno();

        int offsetInicial = tabelaPai.getOffset();

        for (Declaracao declaracao : blocoDeclaracoes.getDeclaracoes()) {
            if (declaracao instanceof DeclaracaoVariavelEmBloco) {
                visitarDeclaracaoDeVariaveisEmBloco((DeclaracaoVariavelEmBloco) declaracao, tabelaDoEscopo);
            } else if (declaracao instanceof ComandoComExpressao) {
                Expressao expressao = ((ComandoComExpressao) declaracao).getExpressao();

                visitarExpressao(expressao, tabelaDoEscopo);
            }
        }

        // Código para reajustar o stack pointer no final do escopo
        gerador.gerar("# reajusta stack bloco:");
        gerador.gerar("addi  $sp, $sp, " + (tabelaDoEscopo.getOffset() - offsetInicial));
    }

    // Após executar uma expressão, seu resultado estará no topo do stack
    @Override
    public void visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo) {
        if (expressao instanceof ExpressaoIdentificador) {
            visitaIdentificador((ExpressaoIdentificador) expressao, tabelaDoEscopo);
        } else if (expressao instanceof ExpressaoInteiroLiteral) {

        }
    }

    // Aqui temos somente variáveis locais
    @Override
    public void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco) {
        int offset = tabelaBloco.getOffset();
        int offsetInicial = offset;

        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                tabelaBloco.adicionaSimbolo(new SimboloVariavelLocal(var, offset));

                tabelaBloco.setOffset(offset);
                offset += getEspacoMemoriaVariavelLocal(var);
            }
        }

        // Reserva espaço para todas as variáveis locais
        gerador.gerar("# reservando espaço variáveis:");
        gerador.gerar("addi  $sp, $sp, -" + (tabelaBloco.getOffset() - offsetInicial));

        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                if (var.isVetor()) {
                    // Aloca o vetor e guarda o endereço em $s0
                    alocaVetor(var);
                }
            }
        }
    }

    @Override
    public void visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabelaDeSimbolos) {
        String nomeVariavel = identificador.getIdentificador();

        // Verifique se a variável existe
        Simbolo<?> simbolo = tabelaDeSimbolos.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            this.programa.reportaErroSemantico("Variável não declarada: " + nomeVariavel, identificador.getToken());
            return;
        }

        // Checar se é uma variável local ou global
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();

            if (variavel.isVetor()) {
                if (identificador.getIndex() != null) {
                    // TODO executar a expressão do index aqui
                    leVariavelGlobalVetorIndexado(variavelGlobal, tabelaDeSimbolos);
                } else {
                    leVariavelGlobal(variavelGlobal.getNoSintatico().getTipo().getTipo(), variavelGlobal.getNome(), tabelaDeSimbolos);
                }
            } else {
                if (identificador.getIndex() != null) {
                    this.programa.reportaErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                    return;
                }
                leVariavelGlobal(variavel.getTipo().getTipo(), variavel.getNome(), tabelaDeSimbolos);
            }
        } else {
            SimboloVariavelLocal variavelLocal = (SimboloVariavelLocal) simbolo;
            Variavel variavel = variavelLocal.getNoSintatico();
            int offset = variavelLocal.getOffset();

            if (variavel.isVetor()) {
                if (identificador.getIndex() != null) {
                    // TODO executar a expressão do index aqui
                    leVariavelLocalVetorIndexado(variavelLocal, tabelaDeSimbolos);
                } else {
                    leVariavelLocal(variavel.getTipo().getTipo(), variavel.getNome(), offset, tabelaDeSimbolos);
                }
            } else {
                if (identificador.getIndex() != null) {
                    this.programa.reportaErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                    return;
                }
                leVariavelLocal(variavel.getTipo().getTipo(), variavel.getNome(), offset, tabelaDeSimbolos);
            }
        }
    }

    @Override
    public void visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos) {
        int valor = expressao.getConteudo();
        gerador.gerar("# empilhando inteiro literal " + valor);
        gerador.gerar("li $s0, " + valor);                    // Carregar valor no registrador $s0
        alocaNoStackEInsereS0(TipoVariavel.INTEIRO, tabelaDeSimbolos); // Empilhar o valor no stack
    }

    @Override
    public void visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos) {
        char valor = expressao.getConteudo();
        gerador.gerar("# empilhando caractere literal " + valor);
        gerador.gerar("li $s0, " + (int) valor);                // Carregar valor ASCII no registrador $ts0
        alocaNoStackEInsereS0(TipoVariavel.CARACTERE, tabelaDeSimbolos); // Empilhar o valor no stack
    }

    @Override
    public void visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos) {
        String valor = expressao.getConteudo();
        String label = gerarLabelUnico(); // Função auxiliar para gerar um label único para a string

        // Adicionar string na seção .data
        gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");

        // Carregar endereço da string e empilhá-lo no stack
        gerador.gerar("# empilhando endereço de string literal");
        gerador.gerar("la $s0, " + label);                    // Carregar endereço no registrador $s0
        alocaNoStackEInsereS0(TipoVariavel.INTEIRO, tabelaDeSimbolos); // Empilhar o endereço no stack
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

    // Assumimos que foi criado espaço no stack para armazenar o endereço do vetor
    private void alocaVetor(Variavel variavel) {
        int tamanho = getEspacoMemoria(variavel.getTipo().getTipo()) * variavel.getTamanhoVetor();

        gerador.gerar("# aloca vetor " + variavel.getNome());
        gerador.gerar("li    $v0, 9");          // Syscall número 9 para alocar memória
        gerador.gerar("li    $a0, " + tamanho); // Carregar tamanho do array em $a0
        gerador.gerar("syscall");               // Executar syscall
        gerador.gerar("move  $s0, $v0");        // Endereço base do array alocado está agora no registrador $s0
        gerador.gerar("sw    $s0, 0($sp)");     // Guarda o endereço do vetor no stack
        gerador.gerar("# fim aloca vetor " + variavel.getNome());
    }

    private void leVariavelGlobal(TipoVariavel tipo, String nome, TabelaDeSimbolos tabelaDeSimbolos) {
        gerador.gerar("# lendo variável global " + nome);
        gerador.gerar("la    $t1, " + nome);          // carrega endereço da variável em $t1
        gerador.gerar("lw    $s0 0($t1)");            // carrega o valor no endereço no registrador passado
        alocaNoStackEInsereS0(tipo, tabelaDeSimbolos);
        gerador.gerar("# fim lendo variável global " + nome);
    }

    private void leVariavelGlobalVetorIndexado(SimboloVariavelGlobal variavelGlobal, TabelaDeSimbolos tabelaDeSimbolos) {
        int tamanhoElemento = getEspacoMemoria(variavelGlobal.getNoSintatico().getTipo().getTipo());

        gerador.gerar("# lendo vetor global indexado " + variavelGlobal.getNome());

        // Carregar o endereço base do vetor global
        gerador.gerar("la    $t1, " + variavelGlobal.getNome());

        // Desempilhar o índice do stack e armazená-lo em $s0
        desalocaNoStackEGuardaEmS0(TipoVariavel.INTEIRO, tabelaDeSimbolos);

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("sll   $t0, $s0, " + Integer.numberOfTrailingZeros(tamanhoElemento)); // Multiplicar índice pelo tamanho do elemento
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor
        gerador.gerar("lw    $s0, 0($t1)");

        // Empilhar o valor do elemento no stack
        alocaNoStackEInsereS0(variavelGlobal.getNoSintatico().getTipo().getTipo(), tabelaDeSimbolos);

        gerador.gerar("# fim lendo vetor global indexado " + variavelGlobal.getNome());
    }

    private void leVariavelLocal(TipoVariavel tipo, String nome, int offset, TabelaDeSimbolos tabelaDeSimbolos) {
        gerador.gerar("# lendo variável local " + nome);
        gerador.gerar("lw    $s0, " + offset + "($fp)"); // Carrega o valor da variável local em $s0
        alocaNoStackEInsereS0(tipo, tabelaDeSimbolos);
        gerador.gerar("# fim lendo variável local " + nome);
    }

    private void leVariavelLocalVetorIndexado(SimboloVariavelLocal variavelLocal, TabelaDeSimbolos tabelaDeSimbolos) {
        int offset = variavelLocal.getOffset();
        int tamanhoElemento = getEspacoMemoria(variavelLocal.getNoSintatico().getTipo().getTipo());
        int tamanhoStackAtual = tabelaDeSimbolos.getOffset();

        gerador.gerar("# lendo vetor local indexado " + variavelLocal.getNome());

        // Calcular o endereço base do vetor no stack
        gerador.gerar("addiu $t1, $sp, " + (tamanhoStackAtual - offset));

        // Desempilhar o índice do stack e armazená-lo em $s0
        desalocaNoStackEGuardaEmS0(TipoVariavel.INTEIRO, tabelaDeSimbolos);
        gerador.gerar("lw    $s0, 0($sp)");
        gerador.gerar("addiu $sp, $sp, 4");

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("sll   $t0, $t0, " + Integer.numberOfTrailingZeros(tamanhoElemento)); // Multiplicar índice pelo tamanho do elemento
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor
        gerador.gerar("lw    $s0, 0($t1)");

        // Empilhar o valor do elemento no stack
        alocaNoStackEInsereS0(variavelLocal.getNoSintatico().getTipo().getTipo(), tabelaDeSimbolos);

        gerador.gerar("# fim lendo vetor local indexado " + variavelLocal.getNome());
    }

    private void alocaNoStackEInsereS0(TipoVariavel tipo, TabelaDeSimbolos tabelaDeSimbolos) {
        int tamanho = getEspacoMemoria(tipo);

        gerador.gerar("addiu $sp, $sp, -" + tamanho); // Aloca espaço no stack
        gerador.gerar("sw    $s0, 0($sp)");           // Guarda a variável (em $s0) no stack

        tabelaDeSimbolos.setOffset(tabelaDeSimbolos.getOffset() + tamanho);
    }

    private void desalocaNoStackEGuardaEmS0(TipoVariavel tipo, TabelaDeSimbolos tabelaDeSimbolos) {
        int tamanho = getEspacoMemoria(tipo);

        gerador.gerar("lw    $s0, 0($sp)");          // Guarda o topo do stack em $s0
        gerador.gerar("addiu $sp, $sp, " + tamanho); // Desaloca espaço no stack

        tabelaDeSimbolos.setOffset(tabelaDeSimbolos.getOffset() - tamanho);
    }
}
