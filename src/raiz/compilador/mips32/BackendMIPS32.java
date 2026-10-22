package src.raiz.compilador.mips32;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.Backend;
import src.raiz.compilador.FuncoesNativas;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.ModoGerador;
import src.raiz.compilador.RotinaRuntime;
import src.raiz.erros.BugCompilador;
import src.raiz.semantica.*;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

// Gera assembly MIPS32 (no dialeto do MARS) a partir de um programa anotado pelo AnalisadorSemantico.
// Toda expressão é avaliada empilhando seu resultado: quem a usa desempilha o valor
public class BackendMIPS32 implements Backend {

    private final GeradorDeCodigo gerador = new GeradorDeCodigoMIPS32();
    private final Map<String, String> stringsParaLabels = new HashMap<>();
    private int contadorLabels = 0;

    // Offset de cada variável local e parâmetro no momento em que foi empilhado, indexado pelo nó da declaração
    private final Map<NoSintatico, Integer> offsetsVariaveis = new IdentityHashMap<>();

    // Se estamos gerando código para uma função, ela está preenchida aqui
    private DeclaracaoFuncao funcaoAtual = null;

    @Override
    public String gerarCodigo(Programa programa) {
        gerarDeclaracoesGlobais(programa.getDeclaracaoFuncoesEVariaveis());
        gerarBlocoPrograma(programa.getBlocoPrograma());

        return gerador.geraCodigoObjeto();
    }

    private void gerarDeclaracoesGlobais(DeclaracaoFuncoesEVariaveis declaracoes) {
        for (DeclaracaoGlobal declaracao : declaracoes.getDeclaracoesEmOrdem()) {
            switch (declaracao) {
                case DeclaracaoDeVariavel declaracaoDeVariavel -> {
                    gerador.setModoAtual(ModoGerador.VARIAVEIS_GLOBAIS);

                    for (Variavel variavel : declaracaoDeVariavel.getVariaveis()) {
                        // O alinhamento garante que inteiros e flutuantes que venham depois de caracteres
                        // fiquem em endereços múltiplos de 4
                        gerador.gerar(".align 2");
                        gerador.gerar(labelVariavelGlobal(variavel.getNome()) + ": .space " + getEspacoMemoriaVariavelGlobal(variavel));
                    }
                }
                case DeclaracaoFuncao funcao -> gerarFuncao(funcao);
            }
        }
    }

    private void gerarFuncao(DeclaracaoFuncao funcao) {
        gerador.setModoAtual(ModoGerador.FUNCAO);

        // Escopo que contém as variáveis locais da função, incluindo os parâmetros de chamada
        EscopoPilha pilhaFuncao = new EscopoPilha();

        gerador.gerar(labelFuncao(funcao.getNome()) + ":");

        // Os argumentos já foram empilhados por quem chamou a função
        for (ParametroFuncao parametro : funcao.getParametros()) {
            offsetsVariaveis.put(parametro, pilhaFuncao.getOffset());
            pilhaFuncao.alteraOffset(4);
        }

        // Guardando endereço de retorno no stack (espaço reservado na chamada)
        gerador.gerar("sw    $ra, " + pilhaFuncao.getOffset() + "($sp)");

        funcaoAtual = funcao;
        gerarBloco(funcao.getCorpo(), pilhaFuncao);

        // Se a função é de retorno vazio, temos que finalizá-la
        if (funcao.getTipoRetorno().isTipoVazio()) {
            finalizarFuncao(pilhaFuncao, funcao.getNome());
        }

        gerador.gerar("# fim função " + funcao.getNome() + "\n");
    }

    private void gerarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.gerar(".globl main");
        gerador.gerar("main:");

        funcaoAtual = null;
        gerarBloco(blocoPrograma.getBlocoDeclaracoes(), new EscopoPilha());

        // Finalizar o programa
        gerador.gerar("li    $v0, 10 # finalizando programa"); // Syscall para saída
        gerador.gerar("syscall       # fim main");             // Executar syscall
    }

    private void gerarBloco(BlocoDeclaracoes bloco, EscopoPilha pilha) {
        gerarVariaveisLocais(bloco.getVariaveis(), pilha);
        for (Comando comando : bloco.getComandos()) {
            gerarComando(comando, pilha);
        }

        if (pilha.getDiferencaOffset() != 0) {
            // Código para reajustar o stack pointer no final do escopo
            gerador.gerar("addiu $sp, $sp, " + pilha.getDiferencaOffset() + " # reajusta stack bloco");
            pilha.alteraOffset(-pilha.getDiferencaOffset());
        }
    }

    private void gerarVariaveisLocais(DeclaracaoVariavelEmBloco declaracaoVariaveis, EscopoPilha pilha) {
        for (DeclaracaoDeVariavel declaracao : declaracaoVariaveis.getDeclaracoesDeVariaveis()) {
            for (Variavel variavel : declaracao.getVariaveis()) {
                offsetsVariaveis.put(variavel, pilha.getOffset());
                pilha.alteraOffset(4);

                gerador.gerar("addiu $sp, $sp, -4 # reservando espaço variável " + variavel.getNome());
                if (variavel.isVetor()) {
                    // Aloca o vetor e guarda o endereço no stack no espaço que acabou de ser reservado
                    alocaVetor(variavel);
                }
            }
        }
    }

    private void gerarComando(Comando comando, EscopoPilha pilha) {
        switch (comando) {
            case ComandoBloco comandoBloco -> gerarBloco(comandoBloco.getDeclaracoes(), pilha.criaBlocoInterno());
            case ComandoEscreva comandoEscreva -> gerarEscreva(comandoEscreva, pilha);
            case ComandoLeia comandoLeia -> gerarLeia(comandoLeia, pilha);
            case ComandoNovalinha ignored -> gerarNovalinha();
            case ComandoVazio ignored -> {
                // Não faz nada
            }
            case ComandoRetorno comandoRetorno -> gerarRetorno(comandoRetorno, pilha);
            case ComandoSe comandoSe -> gerarSe(comandoSe, pilha);
            case ComandoEnquanto comandoEnquanto -> gerarEnquanto(comandoEnquanto, pilha);
            case ComandoComExpressao comandoComExpressao -> {
                gerarExpressao(comandoComExpressao.getExpressao(), pilha);
                // Limpando efeito colateral
                desempilharEmS0(pilha);
            }
        }
    }

    // Gera o código que avalia a expressão e empilha seu resultado
    private void gerarExpressao(Expressao expressao, EscopoPilha pilha) {
        switch (expressao) {
            case ExpressaoEntreParenteses entreParenteses -> gerarExpressao(entreParenteses.getExpressao(), pilha);
            case ExpressaoIdentificador identificador -> gerarIdentificador(identificador, pilha);
            case ExpressaoInteiroLiteral inteiro -> {
                int valor = inteiro.getConteudo();
                gerador.gerar("li    $s0, " + valor + " # empilhando inteiro literal " + valor);
                empilharS0(pilha);
            }
            case ExpressaoFlutuanteLiteral flutuante -> {
                float valor = flutuante.getConteudo();
                // Carrega o valor como um int no padrão IEEE 754
                gerador.gerar("li    $s0, " + Float.floatToRawIntBits(valor) + " # empilhando flutuante literal " + valor);
                empilharS0(pilha);
            }
            case ExpressaoCaractereLiteral caractere -> {
                char valor = caractere.getConteudo();
                gerador.gerar("li    $s0, " + (int) valor + " # empilhando caractere literal " + valor);
                empilharS0(pilha);
            }
            case ExpressaoStringLiteral string -> gerarString(string, pilha);
            case ExpressaoAtribuicao atribuicao -> gerarAtribuicao(
                    atribuicao.getIdentificador(),
                    () -> gerarExpressao(atribuicao.getExpressaoLadoDireito(), pilha),
                    pilha
            );
            case ExpressaoBinaria binaria -> gerarBinaria(binaria, pilha);
            case ExpressaoUnaria unaria -> gerarUnaria(unaria, pilha);
            case ExpressaoTernaria ternaria -> gerarTernaria(ternaria, pilha);
            case ExpressaoChamadaFuncao chamada -> gerarChamada(chamada, pilha);
            case ExpressaoVazio ignored -> {
                // Não produz valor
            }
        }
    }

    private void gerarIdentificador(ExpressaoIdentificador identificador, EscopoPilha pilha) {
        SimboloVariavel simbolo = identificador.getSimbolo();

        if (simbolo instanceof SimboloVariavelGlobal global) {
            if (global.isVetor()) {
                gerarExpressao(identificador.getIndex(), pilha);
                leVariavelGlobalVetorIndexado(global, pilha);
            } else {
                leVariavelGlobal(global, pilha);
            }
        } else if (simbolo.isVetor()) {
            gerarExpressao(identificador.getIndex(), pilha);
            leVariavelLocalVetorIndexado(simbolo, pilha);
        } else {
            gerador.gerar("lw    $s0, " + offsetNaPilha(simbolo, pilha) + "($sp) # lendo variável local " + simbolo.getNome());
            empilharS0(pilha);
            gerador.gerar("# fim lendo variável local " + simbolo.getNome());
        }
    }

    private void gerarString(ExpressaoStringLiteral string, EscopoPilha pilha) {
        String valor = string.getConteudo();
        String label = stringsParaLabels.get(valor);

        if (label == null) {
            label = novoLabel("string");
            // Adicionar string na seção .data
            gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");
            stringsParaLabels.put(valor, label);
        }

        // Carregar endereço da string e empilhá-lo no stack
        gerador.gerar("la    $s0, " + label + " # empilhando endereço de string literal");
        empilharS0(pilha);
    }

    private void gerarNovalinha() {
        gerador.gerar("li    $v0, 11 # novaLinha"); // Syscall para imprimir char
        gerador.gerar("li    $a0, 10");             // ACII do \n
        gerador.gerar("syscall       # fim novaLinha");
    }

    private void gerarBinaria(ExpressaoBinaria expressao, EscopoPilha pilha) {
        OperadorBinario operador = expressao.getOperador();

        if (operador.getCategoria() == OperadorBinario.Categoria.LOGICA) {
            gerarLogica(expressao, operador == OperadorBinario.E ? "beqz" : "bnez", pilha);
            return;
        }

        String nomeOperacao = operador.getDescricao();
        gerador.gerar("# " + nomeOperacao);

        TipoVariavel tipoEsquerdo = expressao.getEsquerda().getTipo();
        TipoVariavel tipoDireito = expressao.getDireita().getTipo();
        gerarExpressao(expressao.getEsquerda(), pilha);
        gerarExpressao(expressao.getDireita(), pilha);

        // Lê o segundo valor em $t1/$f1 e o primeiro em $t0/$f0
        desempilhar(pilha, tipoDireito == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F1 : RegistradoresMIPS32.T1);
        desempilhar(pilha, tipoEsquerdo == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0);

        switch (operador.getCategoria()) {
            case ARITMETICA -> {
                // Quando é feita uma operação entre int e float, o int é convertido para float
                if (expressao.getTipo() == TipoVariavel.FLUTUANTE) {
                    if (tipoEsquerdo != TipoVariavel.FLUTUANTE) {
                        gerador.gerar("mtc1    $t0, $f0 # convertendo lado esquerdo para float");
                        gerador.gerar("cvt.s.w $f0, $f0");
                    } else if (tipoDireito != TipoVariavel.FLUTUANTE) {
                        gerador.gerar("mtc1    $t1, $f1 # convertendo lado direito para float");
                        gerador.gerar("cvt.s.w $f1, $f1");
                    }
                }
                gerarOperacaoAritmetica(expressao, expressao.getTipo());
            }
            case INTEIRA -> gerarOperacaoInteira(expressao);
            case COMPARACAO -> gerarComparacao(operador, tipoEsquerdo);
            case LOGICA -> throw new BugCompilador("Operador lógico já tratado");
        }

        // Guarda o resultado no stack
        empilhar(pilha, expressao.getTipo() == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);
    }

    // Usa os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e coloca o resultado em $t0/$f0
    private void gerarOperacaoAritmetica(ExpressaoBinaria expressao, TipoVariavel tipo) {
        OperadorBinario operador = expressao.getOperador();
        boolean flutuante = tipo == TipoVariavel.FLUTUANTE;

        switch (operador) {
            case SOMA -> gerador.gerar(flutuante
                    ? "add.s $f0, $f0, $f1 # soma $f0 e $f1, resultado em $f0"
                    : "add   $t0, $t0, $t1 # soma $t0 e $s1, resultado em $t0");
            case SUBTRACAO -> gerador.gerar(flutuante
                    ? "sub.s $f0, $f0, $f1 # subtrai $f0 e $f1, resultado em $f0"
                    : "sub   $t0, $t0, $t1 # subtrai $t1 de $t0, resultado em $t0");
            case MULTIPLICACAO -> gerador.gerar(flutuante
                    ? "mul.s $f0, $f0, $f1 # multiplica $f0 e $f1, resultado em $f0"
                    : "mul   $t0, $t0, $t1 # multiplica $t0 por $t1, resultado em $t0");
            case DIVISAO -> {
                if (flutuante) {
                    // Divisão de flutuantes por zero segue o IEEE 754 e resulta em Infinity ou NaN, sem erro
                    gerador.gerar("div.s $f0, $f0, $f1 # divide $f0 por $f1, resultado em $f0");
                } else {
                    gerarChecagemDivisaoPorZero(expressao);
                    gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
                    gerador.gerar("mflo  $t0      # move o resultado da divisão para $t0");
                }
            }
            default -> throw new BugCompilador("Operador '%s' não é aritmético".formatted(operador));
        }
    }

    // Usa os valores em $t0 (esquerdo) e $t1 (direito) e coloca o resultado em $t0
    private void gerarOperacaoInteira(ExpressaoBinaria expressao) {
        if (expressao.getOperador() != OperadorBinario.RESTO) {
            throw new BugCompilador("Operador '%s' não é uma operação inteira".formatted(expressao.getOperador()));
        }

        gerarChecagemDivisaoPorZero(expressao);
        gerador.gerar("div   $t0, $t1 # divide $t0 por $t1");
        gerador.gerar("mfhi  $t0      # move o resto da divisão para $t0");
    }

    // A instrução div do MIPS não gera exceção com divisor zero, o resultado é só indefinido.
    // Por isso checamos o divisor em $t1 antes e, se for zero, encerramos o programa com erro
    private void gerarChecagemDivisaoPorZero(ExpressaoBinaria expressao) {
        String labelOk = novoLabel("divisaoOk");

        gerador.gerar("bnez  $t1, " + labelOk + " # checa divisão por zero");
        gerarErroExecucao("Erro de execução: divisão por zero", expressao.getToken());
        gerador.gerar(labelOk + ":");
    }

    // Desvia para a rotina de runtime que escreve a mensagem no stderr e encerra o programa
    private void gerarErroExecucao(String mensagem, Token token) {
        // A syscall de escrita em arquivo do MARS grava os bytes como estão, então a mensagem é emitida
        // já codificada em UTF-8 (com .asciiz, caracteres acentuados sairiam em Latin-1)
        byte[] bytes = (AstUtil.montaMensagemErro(mensagem, token) + "\n").getBytes(StandardCharsets.UTF_8);
        StringJoiner valores = new StringJoiner(", ");
        for (byte b : bytes) {
            valores.add(String.valueOf(b)); // o MARS espera .byte com sinal, entre -128 e 127
        }

        String labelMensagem = novoLabel("mensagemErro");
        gerador.gerarVarGlobal(labelMensagem + ": .byte " + valores);

        gerador.gerarRotinaRuntime(RotinaRuntime.ERRO_EXECUCAO);
        gerador.gerar("la    $a1, " + labelMensagem);
        gerador.gerar("li    $a2, " + bytes.length);
        gerador.gerar("j     " + RotinaRuntime.ERRO_EXECUCAO.label);
    }

    // Compara os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e coloca 1 ou 0 em $t0
    private void gerarComparacao(OperadorBinario operador, TipoVariavel tipo) {
        if (tipo == TipoVariavel.FLUTUANTE) {
            // O coprocessador só compara com ==, < e <=. Os demais trocam a ordem dos operandos ou invertem o resultado
            switch (operador) {
                case IGUAL -> gerador.gerar("c.eq.s $f0, $f1 # verifica se $f0 == $f1");
                case DIFERENTE -> gerador.gerar("c.eq.s $f0, $f1 # verifica se $f0 != $f1");
                case MENOR -> gerador.gerar("c.lt.s $f0, $f1 # verifica se $f0 < $f1");
                case MAIOR -> gerador.gerar("c.lt.s $f1, $f0 # verifica se $f0 > $f1");
                case MENOR_IGUAL -> gerador.gerar("c.le.s $f0, $f1 # verifica se $f0 <= $f1");
                case MAIOR_IGUAL -> gerador.gerar("c.le.s $f1, $f0 # verifica se $f0 >= $f1");
                default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
            }
            guardaComparacaoFloatEmT0(operador == OperadorBinario.DIFERENTE);
            return;
        }

        switch (operador) {
            case IGUAL -> {
                gerador.gerar("sub   $t0, $t0, $t1 # subtrai $t1 de $t0");
                gerador.gerar("sltiu $t0, $t0, 1   # define $t0 como 1 se $t0 era 0 (iguais), ou 0 caso contrário");
            }
            case DIFERENTE -> {
                gerador.gerar("sub   $t0, $t0, $t1   # subtrai $t1 de $t0");
                gerador.gerar("sltu  $t0, $zero, $t0 # define $t0 como 1 se são diferentes, 0 se iguais");
            }
            case MENOR -> gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1, resultado em $t0");
            case MAIOR -> gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0, resultado em $t0");
            case MENOR_IGUAL -> {
                gerador.gerar("slt   $t0, $t1, $t0 # verifica se $t1 < $t0");
                gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'menor ou igual'");
            }
            case MAIOR_IGUAL -> {
                gerador.gerar("slt   $t0, $t0, $t1 # verifica se $t0 < $t1");
                gerador.gerar("xori  $t0, $t0, 1   # inverte o resultado para obter 'maior ou igual'");
            }
            default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
        }
    }

    private void guardaComparacaoFloatEmT0(boolean inverter) {
        String labelFalso = novoLabel("comparacaoFalso");
        String labelFim = novoLabel("comparacaoFim");

        gerador.gerar("bc1f  " + labelFalso);
        gerador.gerar("li    $t0, " + (inverter ? 0 : 1));
        gerador.gerar("j     " + labelFim);
        gerador.gerar(labelFalso + ":");
        gerador.gerar("li    $t0, " + (inverter ? 1 : 0));
        gerador.gerar(labelFim + ":");
    }

    // Avalia com curto-circuito: se o lado esquerdo já decide o resultado (falso para 'e', verdadeiro para 'ou'),
    // o lado direito não é avaliado. O resultado é sempre normalizado para 0 ou 1
    private void gerarLogica(ExpressaoBinaria expressao, String instrucaoCurtoCircuito, EscopoPilha pilha) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        String labelCurtoCircuito = novoLabel("logicoCurto");

        gerador.gerar("# " + nomeOperacao);

        gerarExpressao(expressao.getEsquerda(), pilha);
        desempilhar(pilha, RegistradoresMIPS32.T0);
        gerador.gerar(instrucaoCurtoCircuito + "  $t0, " + labelCurtoCircuito + " # curto-circuito");

        gerarExpressao(expressao.getDireita(), pilha);
        desempilhar(pilha, RegistradoresMIPS32.T0);

        gerador.gerar(labelCurtoCircuito + ":");
        gerador.gerar("sltu  $t0, $zero, $t0 # normaliza o resultado para 0 ou 1");
        empilhar(pilha, RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);
    }

    private void gerarUnaria(ExpressaoUnaria expressao, EscopoPilha pilha) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        TipoVariavel tipo = expressao.getExpressao().getTipo();
        RegistradoresMIPS32 registrador = tipo == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0;

        gerador.gerar("# inicio " + nomeOperacao);

        gerarExpressao(expressao.getExpressao(), pilha);
        desempilhar(pilha, registrador);

        switch (expressao.getOperador()) {
            case NEGACAO -> gerador.gerar("seq   $t0, $t0, $zero # inverte o valor booleano, 1 se $t0 é 0, 0 caso contrário");
            case NEGATIVO -> gerador.gerar(tipo == TipoVariavel.FLUTUANTE
                    ? "neg.s $f0, $f0 # inverte o sinal do número"
                    : "sub   $t0, $zero, $t0 # inverte o sinal do número");
        }

        empilhar(pilha, registrador);

        gerador.gerar("# fim " + nomeOperacao);
    }

    private void gerarTernaria(ExpressaoTernaria expressao, EscopoPilha pilha) {
        gerador.gerar("# inicio expressão ternária");

        gerarExpressao(expressao.getCondicao(), pilha);
        desempilharEmS0(pilha);

        String labelFalso = novoLabel("ternarioFalso");
        String labelFim = novoLabel("ternarioFim");

        gerador.gerar("beqz  $s0, " + labelFalso);

        gerarExpressao(expressao.getSe(), pilha);
        gerador.gerar("j     " + labelFim);

        // Só um dos ramos executa, então o valor empilhado pelo ramo 'se' não existe quando o 'senao' executa
        pilha.alteraOffset(-4);

        gerador.gerar(labelFalso + ":");
        gerarExpressao(expressao.getSenao(), pilha);

        gerador.gerar(labelFim + ": # fim expressão ternária");
    }

    // Atribui à variável alvo o valor empilhado por gerarValor. O valor continua no stack, pois a atribuição
    // também é uma expressão
    private void gerarAtribuicao(ExpressaoIdentificador alvo, Runnable gerarValor, EscopoPilha pilha) {
        SimboloVariavel simbolo = alvo.getSimbolo();
        TipoVariavel tipo = simbolo.getTipo();
        String nome = simbolo.getNome();

        gerarValor.run();

        if (simbolo instanceof SimboloVariavelGlobal) {
            String label = labelVariavelGlobal(nome);

            if (simbolo.isVetor()) {
                gerador.gerar("la    $t0, " + label + " # carrega endereço do vetor global em $t0 " + nome);
                atribuiAVetor(alvo, tipo, pilha);
            } else {
                gerador.gerar("la    $t1, " + label); // Carregar endereço da variável global

                // Carregar valor do topo do stack e armazenar valor na variável global
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável global " + nome);
                if (tipo == TipoVariavel.CARACTERE) {
                    gerador.gerar("sb    $t0, 0($t1)"); // caracteres globais ocupam um único byte
                } else {
                    gerador.gerar("sw    $t0, 0($t1)");
                }
            }
        } else {
            int offset = offsetNaPilha(simbolo, pilha);

            if (simbolo.isVetor()) {
                gerador.gerar("lw    $t0, " + offset + "($sp) # carrega endereço do vetor em $t0 " + nome);
                atribuiAVetor(alvo, tipo, pilha);
            } else {
                // Carregar valor do topo do stack e armazenar na variável local
                gerador.gerar("lw    $t0, 0($sp) # atribuindo à variável local " + nome);
                gerador.gerar("sw    $t0, " + offset + "($sp)");
            }
        }
    }

    // Endereço do vetor deve estar no registrador $t0
    private void atribuiAVetor(ExpressaoIdentificador alvo, TipoVariavel tipo, EscopoPilha pilha) {
        empilhar(pilha, RegistradoresMIPS32.T0);

        int tamanhoElemento = getEspacoMemoria(tipo);
        gerarExpressao(alvo.getIndex(), pilha);
        desempilharEmS0(pilha); // Desempilha o index
        desempilhar(pilha, RegistradoresMIPS32.T0); // Desempilha o endereço

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

    private void gerarEscreva(ComandoEscreva comandoEscreva, EscopoPilha pilha) {
        TipoVariavel tipo = comandoEscreva.getExpressao().getTipo();
        gerarExpressao(comandoEscreva.getExpressao(), pilha);

        gerador.gerar("# inicio escreva");

        switch (tipo) {
            case INTEIRO -> gerador.gerar("li    $v0, 1");    // Syscall para imprimir inteiro
            case CARACTERE -> gerador.gerar("li    $v0, 11"); // Syscall para imprimir char
            case STRING -> gerador.gerar("li    $v0, 4");     // Syscall para imprimir string
            case FLUTUANTE -> gerador.gerar("li    $v0, 2");  // Syscall para imprimir float
            case VAZIO -> throw new BugCompilador("Escrita de valor vazio passou pela análise semântica");
        }

        // Carregar do topo do stack
        if (tipo == TipoVariavel.FLUTUANTE) {
            gerador.gerar("lwc1  $f12, 0($sp)");
        } else {
            gerador.gerar("lw    $a0, 0($sp)");
        }
        gerador.gerar("syscall # fim escreva");

        desempilharEmS0(pilha);
    }

    // Ler um valor é atribuí-lo à variável: o valor lido é empilhado e então armazenado
    private void gerarLeia(ComandoLeia comandoLeia, EscopoPilha pilha) {
        gerador.gerar("# inicio comando leia");

        ExpressaoIdentificador alvo = comandoLeia.getExpressaoIdentificador();
        gerarAtribuicao(alvo, () -> gerarLeitura(alvo.getSimbolo().getTipo(), pilha), pilha);

        // Desempilha, pois é um comando
        desempilharEmS0(pilha);
    }

    private void gerarLeitura(TipoVariavel tipo, EscopoPilha pilha) {
        switch (tipo) {
            case INTEIRO -> gerador.gerar("li    $v0, 5 # syscall para ler inteiro");
            case CARACTERE -> gerador.gerar("li    $v0, 12 # syscall para ler caractere");
            default -> throw new BugCompilador("Leitura de " + tipo + " passou pela análise semântica");
        }
        gerador.gerar("syscall");

        empilhar(pilha, RegistradoresMIPS32.V0);

        gerador.gerar("# fim comando leia");
    }

    private void gerarChamada(ExpressaoChamadaFuncao chamada, EscopoPilha pilha) {
        gerador.gerar("# inicio chamada da função " + chamada.getNomeFuncao());

        switch (chamada.getFuncao()) {
            case SimboloFuncaoNativa nativa -> gerarChamadaNativa(chamada, nativa.funcao(), pilha);
            case SimboloFuncao funcao -> gerarChamadaFuncao(chamada, funcao.declaracao(), pilha);
        }
    }

    // As funções nativas recebem o argumento em registrador e retornam em $v0
    private void gerarChamadaNativa(ExpressaoChamadaFuncao chamada, FuncoesNativas funcao, EscopoPilha pilha) {
        for (Expressao argumento : chamada.getArgumentos()) {
            gerarExpressao(argumento, pilha);
        }

        gerador.gerarFuncaoNativa(funcao);

        switch (funcao) {
            case RAND -> desempilhar(pilha, RegistradoresMIPS32.A0);
            case PISO -> desempilhar(pilha, RegistradoresMIPS32.F12);
        }
        gerador.gerar("jal   " + funcao.nome + " # chama a função nativa");
        empilhar(pilha, RegistradoresMIPS32.V0);
    }

    // Convenção de chamada: quem chama reserva espaço para o endereço de retorno e empilha os argumentos em ordem.
    // A função chamada limpa tudo isso antes de retornar, deixando o resultado em $v0
    private void gerarChamadaFuncao(ExpressaoChamadaFuncao chamada, DeclaracaoFuncao funcao, EscopoPilha pilha) {
        String nomeFuncao = funcao.getNome();
        List<Expressao> argumentos = chamada.getArgumentos();

        gerador.gerar("addiu $sp, $sp, -4 # reservando espaço para o endereço de retorno da função");
        pilha.alteraOffset(4);

        for (int i = 0; i < argumentos.size(); i++) {
            if (funcao.getParametros().get(i).isVetor()) {
                empilharEnderecoVetor(argumentos.get(i), pilha);
            } else {
                gerarExpressao(argumentos.get(i), pilha);
            }
        }

        gerador.gerar("jal    " + labelFuncao(nomeFuncao) + " # chama a função " + nomeFuncao);
        pilha.alteraOffset(-4 * (argumentos.size() + 1)); // Limpando espaço dos argumentos + endereço retorno
        empilhar(pilha, RegistradoresMIPS32.V0); // armazena o resultado da função no stack

        gerador.gerar("# fim chamada da função " + nomeFuncao);
    }

    // Empilha o endereço base do vetor, que é como vetores são passados para funções
    private void empilharEnderecoVetor(Expressao argumento, EscopoPilha pilha) {
        while (argumento instanceof ExpressaoEntreParenteses entreParenteses) {
            argumento = entreParenteses.getExpressao();
        }
        if (!(argumento instanceof ExpressaoIdentificador identificador)) {
            throw new BugCompilador("Argumento vetor não é um identificador: " + argumento.codigoOriginal());
        }

        SimboloVariavel simbolo = identificador.getSimbolo();
        if (simbolo instanceof SimboloVariavelGlobal) {
            gerador.gerar("la    $s0, " + labelVariavelGlobal(simbolo.getNome()) + " # endereço do vetor global " + simbolo.getNome());
        } else {
            gerador.gerar("lw    $s0, " + offsetNaPilha(simbolo, pilha) + "($sp) # endereço do vetor " + simbolo.getNome());
        }
        empilharS0(pilha);
    }

    private void gerarRetorno(ComandoRetorno comandoRetorno, EscopoPilha pilha) {
        gerador.gerar("# inicio comando retorno");

        Expressao expressao = comandoRetorno.getExpressao();
        gerarExpressao(expressao, pilha);

        if (funcaoAtual == null) {
            // No bloco programa, o valor retornado é o código de saída
            if (expressao.getTipo() != TipoVariavel.VAZIO) {
                desempilhar(pilha, RegistradoresMIPS32.A0);
            } else {
                gerador.gerar("li    $a0, 0"); // Retornando 0
            }

            gerador.gerar("li    $v0, 17 # syscall para finalizar o programa com código de retorno");
            gerador.gerar("syscall       # fim main");
        } else {
            if (!funcaoAtual.getTipoRetorno().isTipoVazio()) {
                desempilhar(pilha, RegistradoresMIPS32.V0); // Carrega o valor de retorno em $v0
            }
            finalizarFuncao(pilha, funcaoAtual.getNome());
        }

        gerador.gerar("# fim comando retorno");
    }

    private void finalizarFuncao(EscopoPilha pilha, String nomeFuncao) {
        // Epílogo da função
        // Código para reajustar o stack pointer ao retornar da função: variáveis locais + parâmetros
        if (pilha.getOffset() != 0) {
            gerador.gerar("addiu $sp, $sp, " + pilha.getOffset() + " # reajusta stack bloco retorno");
        }
        gerador.gerar("lw    $ra, 0($sp) # lê endereço de retorno do stack");
        gerador.gerar("addiu $sp, $sp, 4 # volta o stack para limpar o endereço de retorno");
        gerador.gerar("jr    $ra         # retornando da função " + nomeFuncao);
    }

    private void gerarSe(ComandoSe comandoSe, EscopoPilha pilha) {
        String labelFalso = novoLabel("seFalso");
        String labelFim = novoLabel("seFim");

        gerador.gerar("# inicio comando se");

        // Avalia a expressão condicional
        gerarExpressao(comandoSe.getSe(), pilha);
        desempilhar(pilha, RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFalso se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFalso);

        gerarComando(comandoSe.getConsequencia(), pilha);

        // Pula para o final se um bloco alternativo existir
        if (comandoSe.getAlternativa() != null) {
            gerador.gerar("j     " + labelFim);
        }

        // Label para o bloco alternativo/final se não houver senao
        gerador.gerar(labelFalso + ":");
        if (comandoSe.getAlternativa() != null) {
            gerarComando(comandoSe.getAlternativa(), pilha);
            gerador.gerar(labelFim + ":");
        }

        gerador.gerar("# fim comando se");
    }

    private void gerarEnquanto(ComandoEnquanto comandoEnquanto, EscopoPilha pilha) {
        String labelInicio = novoLabel("enquantoInicio");
        String labelFim = novoLabel("enquantoFim");

        gerador.gerar("# inicio comando enquanto");
        gerador.gerar(labelInicio + ":");

        // Avalia a expressão condicional
        gerarExpressao(comandoEnquanto.getCondicional(), pilha);
        desempilhar(pilha, RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFim se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFim);

        gerarComando(comandoEnquanto.getComando(), pilha);

        // Retorna ao início para avaliar a condição novamente
        gerador.gerar("j     " + labelInicio);

        gerador.gerar(labelFim + ":");
        gerador.gerar("# fim comando enquanto");
    }

    private int getEspacoMemoria(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, STRING -> 4; // int tem 4 bytes -> 32 bits. Uma string é um ponteiro
            case CARACTERE -> 1;       // um caractere é um byte
            case FLUTUANTE -> 4;       // os floats são de precissão única
            case VAZIO -> throw new BugCompilador("Tipo vazio sendo usado como valor!");
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

    private void leVariavelGlobal(SimboloVariavelGlobal variavelGlobal, EscopoPilha pilha) {
        // carrega endereço da variável em $s0
        gerador.gerar("la    $s0, " + labelVariavelGlobal(variavelGlobal.getNome()) + " # lendo variável global " + variavelGlobal.getNome());

        switch (variavelGlobal.getTipo()) {
            case CARACTERE -> {
                gerador.gerar("lb    $s0, 0($s0)");
                empilharS0(pilha);
            }
            case INTEIRO -> {
                gerador.gerar("lw    $s0, 0($s0)");
                empilharS0(pilha);
            }
            case FLUTUANTE -> {
                gerador.gerar("lwc1  $f0, 0($s0)");
                empilhar(pilha, RegistradoresMIPS32.F0);
            }
            default -> throw new BugCompilador("Tipo '%s' não suportado para leitura de variável global".formatted(variavelGlobal.getTipo()));
        }

        gerador.gerar("# fim lendo variável global " + variavelGlobal.getNome());
    }

    private void leVariavelGlobalVetorIndexado(SimboloVariavelGlobal variavelGlobal, EscopoPilha pilha) {
        int tamanhoElemento = getEspacoMemoria(variavelGlobal.getTipo());

        // Carregar o endereço base do vetor global
        gerador.gerar("la    $t0, " + labelVariavelGlobal(variavelGlobal.getNome()) + " # lendo vetor global indexado " + variavelGlobal.getNome());

        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(pilha);

        // Calcular o endereço do elemento específico do vetor
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t1, $t1, $t0"); // Endereço do elemento = endereço base + (índice * tamanho do elemento)

        // Ler o valor do elemento do vetor e empilhar no stack
        switch (variavelGlobal.getTipo()) {
            case CARACTERE -> {
                gerador.gerar("lb    $s0, 0($t1)");
                empilharS0(pilha);
            }
            case INTEIRO -> {
                gerador.gerar("lw    $s0, 0($t1)");
                empilharS0(pilha);
            }
            case FLUTUANTE -> {
                gerador.gerar("lwc1  $f0, 0($t1)");
                empilhar(pilha, RegistradoresMIPS32.F0);
            }
            default -> throw new BugCompilador("Tipo '%s' não suportado para leitura de vetor global".formatted(variavelGlobal.getTipo()));
        }

        gerador.gerar("# fim lendo vetor global indexado " + variavelGlobal.getNome());
    }

    private void leVariavelLocalVetorIndexado(SimboloVariavel variavelLocal, EscopoPilha pilha) {
        int tamanhoElemento = getEspacoMemoria(variavelLocal.getTipo());

        gerador.gerar("# lendo vetor local indexado " + variavelLocal.getNome());

        gerador.gerar("lw    $t0, " + offsetNaPilha(variavelLocal, pilha) + "($sp) # lendo endereço do vetor");
        // Desempilhar o índice do stack e armazená-lo em $s0
        desempilharEmS0(pilha);
        gerador.gerar("li    $t1, " + tamanhoElemento);
        gerador.gerar("mul   $t1, $t1, $s0 # tamanho elemento * index");
        gerador.gerar("add   $t0, $t0, $t1");

        if (tamanhoElemento == 1) {
            gerador.gerar("lb    $s0, 0($t0) # lê byte");
        } else {
            gerador.gerar("lw    $s0, 0($t0) # lê inteiro");
        }
        empilharS0(pilha);

        gerador.gerar("# fim lendo vetor local indexado " + variavelLocal.getNome());
    }

    // Distância do $sp atual até uma variável local ou parâmetro
    private int offsetNaPilha(SimboloVariavel simbolo, EscopoPilha pilha) {
        Integer offset = offsetsVariaveis.get(simbolo.getDeclaracao());
        if (offset == null) {
            throw new BugCompilador("Variável '" + simbolo.getNome() + "' usada sem espaço reservado na pilha");
        }
        return pilha.getOffsetStack(offset);
    }

    private void empilharS0(EscopoPilha pilha) {
        empilhar(pilha, RegistradoresMIPS32.S0);
    }

    private void empilhar(EscopoPilha pilha, RegistradoresMIPS32 registrador) {
        gerador.gerar("addiu $sp, $sp, -4"); // Aloca espaço no stack

        // Guarda a variável no stack
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("swc1  " + registrador.getNome() + ", 0($sp)");
        } else {
            gerador.gerar("sw    " + registrador.getNome() + ", 0($sp)");
        }

        pilha.alteraOffset(4);
    }

    private void desempilharEmS0(EscopoPilha pilha) {
        desempilhar(pilha, RegistradoresMIPS32.S0);
    }

    private void desempilhar(EscopoPilha pilha, RegistradoresMIPS32 registrador) {
        // Guarda o topo do stack no registrador
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("lwc1  " + registrador.getNome() + ", 0($sp)");
        } else {
            gerador.gerar("lw    " + registrador.getNome() + ", 0($sp)");
        }
        gerador.gerar("addiu $sp, $sp, 4"); // Desaloca espaço no stack

        pilha.alteraOffset(-4);
    }

    // Labels internas nunca começam com "f_" ou "g_", que são reservados para funções e variáveis globais
    private String novoLabel(String prefixo) {
        return prefixo + "_" + (++contadorLabels);
    }

    // Prefixo evita conflito com labels internas, com "main" e com nomes reservados do montador
    private String labelFuncao(String nomeFuncao) {
        return "f_" + nomeFuncao;
    }

    private String labelVariavelGlobal(String nomeVariavel) {
        return "g_" + nomeVariavel;
    }
}
