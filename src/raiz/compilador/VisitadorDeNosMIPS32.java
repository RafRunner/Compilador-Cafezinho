package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.ComandoComExpressao;
import src.raiz.ast.comandos.ComandoEscreva;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.tabeladesimbolos.*;
import src.raiz.erros.BugCompilador;
import src.raiz.erros.ErroSemantico;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// BlocoDeclaracoes, BlocoPrograma, Declaracao, Comando ComandoBloco, ComandoComExpressao, ComandoEnquanto, ComandoLeia,
// ComandoNovaLinha, ComandoRetorno, ComandoSe, DeclaracaoDeVariavel, DeclaracaoFuncao, DeclaracaoFuncaoEVariaveis,
// DeclaracaoVariaveisEmBloco, Expressao, ExpressaoAtribuicao, ExpressaoBinaria, ExpressaoCaractereLiteral, ComandoEscreva
// ExpressaoChamadaFuncao, ExpressaoDiferente, ExpressaoDivisao, ExpressaoE, ExpressaoEntreparenteses, ExpressaoIdentificado,
// ExpressaoIgual, ExpressaoInteiroLiteral, ExpressaoMaior, ExpressaoLiteral, ExpressaoMaiorIgual, ExpressaoMais, ExpressaoMenor,
// ExpressaoMenorIgual, ExpressaoMenos, Expressaonegativo, ExpressaoNegacao, ExpressaoOu, ExpressaoOu, ExpressaoResto,
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
            } else if (declaracao instanceof ComandoEscreva) {
                visitarComandoEscreva((ComandoEscreva) declaracao, tabelaDoEscopo);
            } else if (declaracao instanceof ComandoComExpressao) {
                Expressao expressao = ((ComandoComExpressao) declaracao).getExpressao();

                TipoVariavel tipo = visitarExpressao(expressao, tabelaDoEscopo);
                // Limpando efeito colateral
                desalocaNoStackEGuardaEmS0(tipo, tabelaDoEscopo);
            }
        }

        // Código para reajustar o stack pointer no final do escopo
        gerador.gerar("# reajusta stack bloco:");
        gerador.gerar("addi  $sp, $sp, " + (tabelaDoEscopo.getOffset() - offsetInicial));
    }

    // Após executar uma expressão, seu resultado estará no topo do stack
    @Override
    public TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo) {
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

        // TODO lançar erro aqui, expressão não identificada
        return TipoVariavel.INTEIRO;
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
    public TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela) {
        String nomeVariavel = identificador.getIdentificador();

        // Verifique se a variável existe
        Simbolo<?> simbolo = tabela.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nomeVariavel, identificador.getToken());
        }

        // Checar se é uma variável local ou global
        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            SimboloVariavelGlobal variavelGlobal = (SimboloVariavelGlobal) simbolo;
            Variavel variavel = variavelGlobal.getNoSintatico();
            TipoVariavel tipoVariavel = variavel.getTipo().getTipo();

            if (variavel.isVetor()) {
                if (identificador.getIndex() != null) {
                    // TODO executar a expressão do index aqui
                    leVariavelGlobalVetorIndexado(variavelGlobal, tabela);
                } else {
                    leVariavelGlobal(tipoVariavel, variavelGlobal.getNome(), tabela);
                }
            } else {
                if (identificador.getIndex() != null) {
                    throw new ErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                }
                leVariavelGlobal(tipoVariavel, variavel.getNome(), tabela);
            }

            return tipoVariavel;
        } else {
            // TODO tratar casos de parâmetros de função
            SimboloVariavelLocal variavelLocal = (SimboloVariavelLocal) simbolo;
            Variavel variavel = variavelLocal.getNoSintatico();
            int offset = variavelLocal.getOffset();
            TipoVariavel tipoVariavel = variavel.getTipo().getTipo();

            if (variavel.isVetor()) {
                if (identificador.getIndex() != null) {
                    // TODO executar a expressão do index aqui
                    leVariavelLocalVetorIndexado(variavelLocal, tabela);
                } else {
                    leVariavelLocal(tipoVariavel, variavel.getNome(), offset, tabela);
                }
            } else {
                if (identificador.getIndex() != null) {
                    throw new ErroSemantico("Variável não é um vetor, não pode ser indexada: " + nomeVariavel, identificador.getToken());
                }
                leVariavelLocal(tipoVariavel, variavel.getNome(), offset, tabela);
            }

            return tipoVariavel;

        }
    }

    @Override
    public TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela) {
        int valor = expressao.getConteudo();
        gerador.gerar("# empilhando inteiro literal " + valor);
        gerador.gerar("li $s0, " + valor);                    // Carregar valor no registrador $s0
        alocaNoStackEInsereS0(TipoVariavel.INTEIRO, tabela); // Empilhar o valor no stack

        return TipoVariavel.INTEIRO;
    }

    @Override
    public TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela) {
        char valor = expressao.getConteudo();
        gerador.gerar("# empilhando caractere literal " + valor);
        gerador.gerar("li $s0, " + (int) valor);                // Carregar valor ASCII no registrador $ts0
        alocaNoStackEInsereS0(TipoVariavel.CARACTERE, tabela); // Empilhar o valor no stack

        return TipoVariavel.CARACTERE;
    }

    @Override
    public TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela) {
        String valor = expressao.getConteudo();
        String label = gerarLabelUnico(valor); // Função auxiliar para gerar um label único para a string

        if (!stringsParaLabels.containsKey(valor)) {
            // Adicionar string na seção .data
            gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");
            stringsParaLabels.put(valor, label);
        }

        // Carregar endereço da string e empilhá-lo no stack
        gerador.gerar("# empilhando endereço de string literal");
        gerador.gerar("la $s0, " + label);         // Carregar endereço no registrador $s0
        alocaNoStackEInsereS0(TipoVariavel.STRING, tabela); // Empilhar o endereço no stack

        return TipoVariavel.STRING;
    }

    public TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela) {
        String nomeVariavel = expressaoAtribuicao.getIdentificador().getIdentificador();
        Simbolo<?> simbolo = tabela.getSimbolo(nomeVariavel);

        if (simbolo == null) {
            throw new ErroSemantico("Variável não declarada: " + nomeVariavel, expressaoAtribuicao.getToken());
        }

        Expressao ladoDireito = expressaoAtribuicao.getExpressaoLadoDireito();
        visitarExpressao(ladoDireito, tabela);

        // Não desempilhamos aqui pois desejamos que o valor continue no stack

        if (simbolo.getTipoSimbolo() == TipoSimbolo.VARIAVEL_GLOBAL) {
            // Atribuir valor a uma variável global
            Variavel variavel = ((SimboloVariavelGlobal) simbolo).getNoSintatico();

            gerador.gerar("# atribuindo à variável global " + variavel.getNome());
            gerador.gerar("lw $t0, 0($sp)");      // Carregar valor do topo do stack
            gerador.gerar("la $t1, " + nomeVariavel); // Carregar endereço da variável global
            gerador.gerar("sw $t0, 0($t1)");      // Armazenar valor na variável global

            return variavel.getTipo().getTipo();
        } else {
            // Atribuir valor a uma variável local
            Variavel variavel = ((SimboloVariavelLocal) simbolo).getNoSintatico();
            int offset = ((SimboloVariavelLocal) simbolo).getOffset();

            gerador.gerar("# atribuindo à variável local " + variavel.getNome());
            gerador.gerar("lw $t0, 0($sp)");      // Carregar valor do topo do stack
            gerador.gerar("sw $t0, " + offset + "($fp)"); // Armazenar valor na variável local

            return variavel.getTipo().getTipo();
        }
    }

    public void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela) {
        TipoVariavel tipo = visitarExpressao(comandoEscreva.getExpressao(), tabela);

        gerador.gerar("# inicio escreva");

        switch (tipo) {
            case INTEIRO:
                gerador.gerar("li $v0, 1");      // Syscall para imprimir inteiro
                gerador.gerar("lw $a0, 0($sp)"); // Carregar o inteiro do topo do stack
                break;
            case CARACTERE:
                // Código para imprimir caractere
                break;
            case STRING:
                // Código para imprimir string
                break;
        }

        gerador.gerar("syscall");
        gerador.gerar("# fim escreva");

        desalocaNoStackEGuardaEmS0(tipo, tabela);
    }

    // TODO criar tipo ponteiro
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

    private void leVariavelGlobal(TipoVariavel tipo, String nome, TabelaDeSimbolos tabela) {
        gerador.gerar("# lendo variável global " + nome);
        gerador.gerar("la    $t1, " + nome);          // carrega endereço da variável em $t1
        gerador.gerar("lw    $s0 0($t1)");            // carrega o valor no endereço no registrador passado
        alocaNoStackEInsereS0(tipo, tabela);
        gerador.gerar("# fim lendo variável global " + nome);
    }

    private void leVariavelGlobalVetorIndexado(SimboloVariavelGlobal variavelGlobal, TabelaDeSimbolos tabela) {
        int tamanhoElemento = getEspacoMemoria(variavelGlobal.getNoSintatico().getTipo().getTipo());

        gerador.gerar("# lendo vetor global indexado " + variavelGlobal.getNome());

        // Carregar o endereço base do vetor global
        gerador.gerar("la    $t1, " + variavelGlobal.getNome());

        // Desempilhar o índice do stack e armazená-lo em $s0
        desalocaNoStackEGuardaEmS0(TipoVariavel.INTEIRO, tabela);

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("sll   $t0, $s0, " + Integer.numberOfTrailingZeros(tamanhoElemento)); // Multiplicar índice pelo tamanho do elemento
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor
        gerador.gerar("lw    $s0, 0($t1)");

        // Empilhar o valor do elemento no stack
        alocaNoStackEInsereS0(variavelGlobal.getNoSintatico().getTipo().getTipo(), tabela);

        gerador.gerar("# fim lendo vetor global indexado " + variavelGlobal.getNome());
    }

    private void leVariavelLocal(TipoVariavel tipo, String nome, int offset, TabelaDeSimbolos tabela) {
        gerador.gerar("# lendo variável local " + nome);
        gerador.gerar("lw    $s0, " + offset + "($fp)"); // Carrega o valor da variável local em $s0
        alocaNoStackEInsereS0(tipo, tabela);
        gerador.gerar("# fim lendo variável local " + nome);
    }

    private void leVariavelLocalVetorIndexado(SimboloVariavelLocal variavelLocal, TabelaDeSimbolos tabela) {
        int offset = variavelLocal.getOffset();
        int tamanhoElemento = getEspacoMemoria(variavelLocal.getNoSintatico().getTipo().getTipo());
        int tamanhoStackAtual = tabela.getOffset();

        gerador.gerar("# lendo vetor local indexado " + variavelLocal.getNome());

        // Calcular o endereço base do vetor no stack
        gerador.gerar("addiu $t1, $sp, " + (tamanhoStackAtual - offset));

        // Desempilhar o índice do stack e armazená-lo em $s0
        desalocaNoStackEGuardaEmS0(TipoVariavel.INTEIRO, tabela);
        gerador.gerar("lw    $s0, 0($sp)");
        gerador.gerar("addiu $sp, $sp, 4");

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("sll   $t0, $t0, " + Integer.numberOfTrailingZeros(tamanhoElemento)); // Multiplicar índice pelo tamanho do elemento
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor
        gerador.gerar("lw    $s0, 0($t1)");

        // Empilhar o valor do elemento no stack
        alocaNoStackEInsereS0(variavelLocal.getNoSintatico().getTipo().getTipo(), tabela);

        gerador.gerar("# fim lendo vetor local indexado " + variavelLocal.getNome());
    }

    private void alocaNoStackEInsereS0(TipoVariavel tipo, TabelaDeSimbolos tabela) {
        int tamanho = getEspacoMemoria(tipo);

        gerador.gerar("addiu $sp, $sp, -" + tamanho); // Aloca espaço no stack
        gerador.gerar("sw    $s0, 0($sp)");           // Guarda a variável (em $s0) no stack

        tabela.setOffset(tabela.getOffset() + tamanho);
    }

    private void desalocaNoStackEGuardaEmS0(TipoVariavel tipo, TabelaDeSimbolos tabela) {
        int tamanho = getEspacoMemoria(tipo);

        gerador.gerar("lw    $s0, 0($sp)");          // Guarda o topo do stack em $s0
        gerador.gerar("addiu $sp, $sp, " + tamanho); // Desaloca espaço no stack

        tabela.setOffset(tabela.getOffset() - tamanho);
    }

    private String gerarLabelUnico(String string) {
        return String.valueOf(string.hashCode());
    }
}
