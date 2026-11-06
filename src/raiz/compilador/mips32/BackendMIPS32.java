package src.raiz.compilador.mips32;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.Backend;
import src.raiz.compilador.ModoGerador;
import src.raiz.erros.BugCompilador;
import src.raiz.semantica.*;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

// Gera assembly MIPS32 (no dialeto do MARS) a partir de um programa anotado pelo AnalisadorSemantico.
//
// Convenções do código gerado:
// - Toda expressão empilha exatamente um valor, e quem a usa o desempilha. Na pilha, todo valor ocupa uma palavra
//   (4 bytes), seja int, flut, car ou endereço
// - Parâmetros e variáveis locais são acessados em relação ao $fp (ver FrameMIPS32), que fica fixo durante toda a
//   função. O $sp só se move para os valores temporários das expressões
// - $s0 e $t0-$t3 são de rascunho: não guardam nada entre um comando e outro. $a0-$a2 levam argumentos para as
//   rotinas de runtime e funções nativas, e $v0 traz o resultado de funções
public class BackendMIPS32 implements Backend {

    private static final int MAX_PALAVRAS_ZERADAS_NO_LUGAR = 16;

    private final GeradorDeCodigoMIPS32 gerador = new GeradorDeCodigoMIPS32();
    private final Map<String, String> stringsParaLabels = new HashMap<>();
    private final Map<ComandoEnquanto, LabelsPulo> labelsDosLacos = new HashMap<>();
    private int contadorLabels = 0;

    // Layout do frame da função (ou do bloco programa) cujo código está sendo gerado
    private FrameMIPS32 frameAtual;

    // Função cujo código está sendo gerado; null no bloco programa
    private DeclaracaoFuncao funcaoAtual = null;

    @Override
    public String gerarCodigo(Programa programa) {
        labelsDosLacos.clear();
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
                        gerador.diretiva(".align 2");
                        gerador.diretiva(
                                labelVariavelGlobal(variavel.getNome()) + ": .space "
                                        + FrameMIPS32.tamanhoVariavel(variavel.getSimbolo())
                        );
                    }
                }
                case DeclaracaoFuncao funcao -> gerarFuncao(funcao);
            }
        }
    }

    private void gerarFuncao(DeclaracaoFuncao funcao) {
        gerador.setModoAtual(ModoGerador.FUNCAO);

        gerador.label(labelFuncao(funcao.getNome()));

        // Os argumentos já foram empilhados por quem chamou a função
        frameAtual = FrameMIPS32.deFuncao(funcao);
        // Prólogo: salva $ra e o $fp de quem chamou, aponta o $fp para eles e reserva o espaço das variáveis locais
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -8)
                .comentario("espaço para o $ra e o $fp antigo");
        gerador.instrucao("sw", RegMIPS32.RA, new Deslocamento(RegMIPS32.SP, 4))
                .comentario("guarda endereço de retorno");
        gerador.instrucao("sw", RegMIPS32.FP, new Deslocamento(RegMIPS32.SP, 0))
                .comentario("guarda o $fp de quem chamou");
        gerador.instrucao("move", RegMIPS32.FP, RegMIPS32.SP)
                .comentario("o frame da função começa aqui");
        reservaFrame();

        funcaoAtual = funcao;
        gerarBloco(funcao.getCorpo());

        // Se a função é de retorno vazio, temos que finalizá-la
        if (funcao.getTipoRetorno().isTipoVazio()) {
            finalizarFuncao(funcao.getNome());
        }

        gerador.comentario("fim função " + funcao.getNome());
    }

    private void gerarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        frameAtual = FrameMIPS32.dePrograma(blocoPrograma);

        gerador.diretiva(".globl main");
        gerador.label("main");
        gerador.instrucao("move", RegMIPS32.FP, RegMIPS32.SP)
                .comentario("o frame do programa começa aqui");
        reservaFrame();

        funcaoAtual = null;
        gerarBloco(blocoPrograma.getBlocoDeclaracoes());

        // Chegar ao fim do bloco programa encerra o programa com código de saída 0
        gerador.instrucao("li", RegMIPS32.V0, 10).comentario("syscall para encerrar o programa");
        gerador.instrucao("syscall").comentario("fim main");
    }

    // Reserva de uma vez o espaço de todas as variáveis locais do frame atual. Blocos não reservam nem liberam espaço
    private void reservaFrame() {
        int tamanho = frameAtual.getTamanho();
        if (tamanho > 0) {
            gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -tamanho)
                    .comentario("reserva o frame");
        }
    }

    private void gerarBloco(BlocoDeclaracoes bloco) {
        gerarVariaveisLocais(bloco.getVariaveis());
        for (Comando comando : bloco.getComandos()) {
            gerarComando(comando);
        }
    }

    private void gerarVariaveisLocais(DeclaracaoVariavelEmBloco declaracaoVariaveis) {
        for (DeclaracaoDeVariavel declaracao : declaracaoVariaveis.getDeclaracoesDeVariaveis()) {
            for (Variavel variavel : declaracao.getVariaveis()) {
                if (variavel.isVetor()) {
                    // Um vetor local começa zerado toda vez que o bloco começa: o espaço dele no frame pode ter
                    // valores de uma iteração anterior de um laço ou de um bloco irmão que usou o mesmo espaço
                    zerarVetor(variavel);
                }
            }
        }
    }

    // Zera o intervalo [início, fim) do vetor no frame, palavra por palavra: o tamanho é múltiplo de 4. Como os
    // compiladores reais fazem com o memset, vetores de até MAX_PALAVRAS_ZERADAS_NO_LUGAR palavras são zerados com um
    // sw por palavra (w instruções, contra 3w + 4 da rotina). Os maiores chamam a rotina, para não gerar código demais.
    // Sobrescrever o $ra com o jal não tem problema: o prólogo já o salvou
    private void zerarVetor(Variavel variavel) {
        int inicio = frameAtual.offset(variavel);
        int fim = inicio + FrameMIPS32.tamanhoVariavel(variavel.getSimbolo());

        // TODO: pular esse passo se for provado que o programa popula o vetor antes de usar (#18)
        int palavras = (fim - inicio) / 4;
        if (palavras <= MAX_PALAVRAS_ZERADAS_NO_LUGAR) {
            for (int i = inicio; i < fim; i += 4) {
                gerador.instrucao("sw", RegMIPS32.ZERO, new Deslocamento(RegMIPS32.FP, i))
                        .comentario("zera o vetor " + variavel.getNome());
            }
        } else {
            gerador.incluirRotina(RotinaMIPS32.ZERA_VETOR);
            gerador.instrucao("addiu", RegMIPS32.A0, RegMIPS32.FP, inicio)
                    .comentario("início do vetor " + variavel.getNome());
            gerador.instrucao("addiu", RegMIPS32.A1, RegMIPS32.FP, fim)
                    .comentario("fim do vetor " + variavel.getNome());
            gerador.instrucao("jal", RotinaMIPS32.ZERA_VETOR.label).comentario("zera o vetor " + variavel.getNome());
        }
    }

    private void gerarComando(Comando comando) {
        switch (comando) {
            case ComandoBloco comandoBloco -> gerarBloco(comandoBloco.getDeclaracoes());
            case ComandoEscreva comandoEscreva -> gerarEscreva(comandoEscreva);
            case ComandoLeia comandoLeia -> gerarLeia(comandoLeia);
            case ComandoNovalinha ignored -> gerarNovalinha();
            case ComandoContinue comandoContinue -> gerarContinue(comandoContinue);
            case ComandoPare comandoPare -> gerarPare(comandoPare);
            case ComandoVazio ignored -> {
                // Não faz nada
            }
            case ComandoRetorno comandoRetorno -> gerarRetorno(comandoRetorno);
            case ComandoSe comandoSe -> gerarSe(comandoSe);
            case ComandoEnquanto comandoEnquanto -> gerarEnquanto(comandoEnquanto);
            case ComandoComExpressao comandoComExpressao -> {
                // Ex: uma atribuição ou chamada usada como comando. O valor que a expressão empilhou não é usado
                gerarExpressao(comandoComExpressao.getExpressao());
                desempilharEmS0();
            }
        }
    }

    // Gera o código que avalia a expressão e empilha seu resultado
    private void gerarExpressao(Expressao expressao) {
        switch (expressao) {
            case ExpressaoIdentificador identificador -> gerarIdentificador(identificador);
            case ExpressaoInteiroLiteral inteiro -> {
                int valor = inteiro.getConteudo();
                gerador.instrucao("li", RegMIPS32.S0, valor)
                        .comentario("empilhando inteiro literal " + valor);
                empilharS0();
            }
            case ExpressaoFlutuanteLiteral flutuante -> {
                float valor = flutuante.getConteudo();
                // Carrega o valor como um int no padrão IEEE 754
                gerador.instrucao("li", RegMIPS32.S0, Float.floatToRawIntBits(valor))
                        .comentario("empilhando flutuante literal " + valor);
                empilharS0();
            }
            case ExpressaoCaractereLiteral caractere -> {
                char valor = caractere.getConteudo();
                gerador.instrucao("li", RegMIPS32.S0, (int) valor)
                        .comentario("empilhando caractere literal " + valor);
                empilharS0();
            }
            case ExpressaoStringLiteral string -> gerarString(string);
            case ExpressaoAtribuicao atribuicao ->
                gerarAtribuicao(
                        atribuicao.getIdentificador(), () -> gerarExpressao(atribuicao.getExpressaoLadoDireito())
                );
            case ExpressaoBinaria binaria -> gerarBinaria(binaria);
            case ExpressaoUnaria unaria -> gerarUnaria(unaria);
            case ExpressaoTernaria ternaria -> gerarTernaria(ternaria);
            case ExpressaoChamadaFuncao chamada -> gerarChamada(chamada);
            case ExpressaoVazio ignored -> {
                // Não produz valor
            }
        }
    }

    // Operando de memória de uma variável escalar, pronto para um lw/sw: o label da global (o MARS expande
    // "lw $t0, g_x" em duas instruções) ou o offset em relação ao $fp
    private Object enderecoEscalar(SimboloVariavel simbolo) {
        return switch (simbolo) {
            case SimboloVariavelGlobal global -> labelVariavelGlobal(global.getNome());
            case SimboloVariavelLocal local -> new Deslocamento(RegMIPS32.FP, frameAtual.offset(local));
            case SimboloParametro parametro -> new Deslocamento(RegMIPS32.FP, frameAtual.offset(parametro));
        };
    }

    // Avalia o índice e deixa em $t0 o endereço do elemento: base + índice * tamanho do elemento.
    // Toda leitura e escrita de elemento de vetor passa por aqui, então é aqui que a checagem de limites da #20 entra
    private void calculaEnderecoElemento(ExpressaoIdentificador vetor) {
        SimboloVariavel simbolo = vetor.getSimbolo();
        gerarExpressao(vetor.getIndex());
        desempilharEmS0();
        // A base é carregada depois do índice: uma chamada de função dentro do índice estragaria o $t0
        carregaEnderecoVetor(simbolo, RegMIPS32.T0);
        // Elementos de 1 byte (car) não precisam de multiplicação
        if (FrameMIPS32.getEspacoMemoria(simbolo.getTipo()) == 4) {
            gerador.instrucao("sll", RegMIPS32.S0, RegMIPS32.S0, 2).comentario("índice * 4 bytes");
        }
        gerador.instrucao("addu", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.S0)
                .comentario("endereço de " + simbolo.getNome() + "[índice]");
    }

    // Empilha o valor de uma variável ou de um elemento de vetor
    private void gerarIdentificador(ExpressaoIdentificador identificador) {
        SimboloVariavel simbolo = identificador.getSimbolo();
        Object endereco = new Deslocamento(RegMIPS32.T0, 0);
        if (simbolo.isVetor()) {
            calculaEnderecoElemento(identificador);
        } else {
            endereco = enderecoEscalar(simbolo);
        }
        gerador.instrucao(instrucaoLeitura(simbolo.getTipo()), RegMIPS32.S0, endereco)
                .comentario("lendo " + simbolo.getNome());
        empilharS0();
    }

    // Na memória um car ocupa 1 byte, e os demais tipos uma palavra. Valores flut são copiados como bits, sem passar
    // pelo coprocessador. O car é lido com lbu, sem estender o sinal, para valer o mesmo em qualquer lugar (ver #22)
    private String instrucaoLeitura(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, FLUTUANTE -> "lw";
            case CARACTERE -> "lbu";
            case STRING, VAZIO -> throw new BugCompilador("Leitura não suportada para " + tipo);
        };
    }

    private void gerarString(ExpressaoStringLiteral string) {
        String valor = string.getConteudo();
        String label = stringsParaLabels.get(valor);

        if (label == null) {
            label = novoLabel("string");
            // Adicionar string na seção .data
            gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");
            stringsParaLabels.put(valor, label);
        }

        // Carregar endereço da string e empilhá-lo no stack
        gerador.instrucao("la", RegMIPS32.S0, label).comentario("empilhando endereço de string literal");
        empilharS0();
    }

    private void gerarNovalinha() {
        gerador.instrucao("li", RegMIPS32.V0, 11).comentario("novaLinha");
        gerador.instrucao("li", RegMIPS32.A0, 10);
        gerador.instrucao("syscall").comentario("fim novaLinha");
    }

    private void gerarBinaria(ExpressaoBinaria expressao) {
        OperadorBinario operador = expressao.getOperador();

        if (operador.getCategoria() == OperadorBinario.Categoria.LOGICA) {
            gerarLogica(expressao, operador == OperadorBinario.E ? "beqz" : "bnez");
            return;
        }

        String nomeOperacao = operador.getDescricao();
        gerador.comentario(nomeOperacao);

        TipoVariavel tipoEsquerdo = expressao.getEsquerda().getTipo();
        TipoVariavel tipoDireito = expressao.getDireita().getTipo();
        gerarExpressao(expressao.getEsquerda());
        gerarExpressao(expressao.getDireita());

        // Lê o segundo valor em $t1/$f1 e o primeiro em $t0/$f0
        desempilhar(tipoDireito.isFlutuante() ? RegMIPS32.F1 : RegMIPS32.T1);
        desempilhar(tipoEsquerdo.isFlutuante() ? RegMIPS32.F0 : RegMIPS32.T0);

        switch (operador.getCategoria()) {
            case ARITMETICA -> {
                // Quando é feita uma operação entre int e float, o int é convertido para float
                if (expressao.getTipo().isFlutuante()) {
                    if (tipoEsquerdo != TipoVariavel.FLUTUANTE) {
                        gerador.instrucao("mtc1", RegMIPS32.T0, RegMIPS32.F0)
                                .comentario("convertendo lado esquerdo para float");
                        gerador.instrucao("cvt.s.w", RegMIPS32.F0, RegMIPS32.F0);
                    } else if (tipoDireito != TipoVariavel.FLUTUANTE) {
                        gerador.instrucao("mtc1", RegMIPS32.T1, RegMIPS32.F1)
                                .comentario("convertendo lado direito para float");
                        gerador.instrucao("cvt.s.w", RegMIPS32.F1, RegMIPS32.F1);
                    }
                }
                gerarOperacaoAritmetica(expressao, expressao.getTipo());
            }
            case INTEIRA -> gerarOperacaoInteira(expressao);
            case COMPARACAO -> gerarComparacao(operador, tipoEsquerdo);
            case LOGICA -> throw new BugCompilador("Operador lógico já tratado");
        }

        // Guarda o resultado no stack
        empilhar(expressao.getTipo().isFlutuante() ? RegMIPS32.F0 : RegMIPS32.T0);

        gerador.comentario("fim " + nomeOperacao);
    }

    // Usa os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e coloca o resultado em $t0/$f0
    private void gerarOperacaoAritmetica(ExpressaoBinaria expressao, TipoVariavel tipo) {
        OperadorBinario operador = expressao.getOperador();
        boolean flutuante = tipo.isFlutuante();

        switch (operador) {
            case SOMA -> {
                if (flutuante) {
                    gerador.instrucao("add.s", RegMIPS32.F0, RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("soma");
                } else {
                    gerador.instrucao("add", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                            .comentario("soma");
                }
            }
            case SUBTRACAO -> {
                if (flutuante) {
                    gerador.instrucao("sub.s", RegMIPS32.F0, RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("subtração");
                } else {
                    gerador.instrucao("sub", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                            .comentario("subtração");
                }
            }
            case MULTIPLICACAO -> {
                if (flutuante) {
                    gerador.instrucao("mul.s", RegMIPS32.F0, RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("multiplicação");
                } else {
                    gerador.instrucao("mul", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                            .comentario("multiplicação");
                }
            }
            case DIVISAO -> {
                if (flutuante) {
                    // Divisão de flutuantes por zero segue o IEEE 754 e resulta em Infinity ou NaN, sem erro
                    gerador.instrucao("div.s", RegMIPS32.F0, RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("divide $f0 por $f1, resultado em $f0");
                } else {
                    gerarChecagemDivisaoPorZero(expressao);
                    gerador.instrucao("div", RegMIPS32.T0, RegMIPS32.T1)
                            .comentario("divide $t0 por $t1");
                    gerador.instrucao("mflo", RegMIPS32.T0)
                            .comentario("move o resultado da divisão para $t0");
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
        gerador.instrucao("div", RegMIPS32.T0, RegMIPS32.T1).comentario("divide $t0 por $t1");
        gerador.instrucao("mfhi", RegMIPS32.T0).comentario("move o resto da divisão para $t0");
    }

    // A instrução div do MIPS não gera exceção com divisor zero, o resultado é só indefinido.
    // Por isso checamos o divisor em $t1 antes e, se for zero, encerramos o programa com erro
    private void gerarChecagemDivisaoPorZero(ExpressaoBinaria expressao) {
        String labelOk = novoLabel("divisaoOk");

        gerador.instrucao("bnez", RegMIPS32.T1, labelOk).comentario("checa divisão por zero");
        gerarErroExecucao("Erro de execução: divisão por zero", expressao.getToken());
        gerador.label(labelOk);
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

        gerador.incluirRotina(RotinaMIPS32.ERRO_EXECUCAO);
        gerador.instrucao("la", RegMIPS32.A1, labelMensagem);
        gerador.instrucao("li", RegMIPS32.A2, bytes.length);
        gerador.instrucao("j", RotinaMIPS32.ERRO_EXECUCAO.label);
    }

    // Compara os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e coloca 1 ou 0 em $t0
    private void gerarComparacao(OperadorBinario operador, TipoVariavel tipo) {
        if (tipo.isFlutuante()) {
            // O coprocessador só compara com ==, < e <=. Os demais trocam a ordem dos operandos ou invertem o resultado
            switch (operador) {
                case IGUAL, DIFERENTE -> {
                    gerador.instrucao("c.eq.s", RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("compara floats");
                }
                case MENOR -> {
                    gerador.instrucao("c.lt.s", RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("verifica se $f0 < $f1");
                }
                case MAIOR -> {
                    gerador.instrucao("c.lt.s", RegMIPS32.F1, RegMIPS32.F0)
                            .comentario("verifica se $f0 > $f1");
                }
                case MENOR_IGUAL -> {
                    gerador.instrucao("c.le.s", RegMIPS32.F0, RegMIPS32.F1)
                            .comentario("verifica se $f0 <= $f1");
                }
                case MAIOR_IGUAL -> {
                    gerador.instrucao("c.le.s", RegMIPS32.F1, RegMIPS32.F0)
                            .comentario("verifica se $f0 >= $f1");
                }
                default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
            }
            guardaComparacaoFloatEmT0(operador == OperadorBinario.DIFERENTE);
            return;
        }

        switch (operador) {
            case IGUAL -> {
                gerador.instrucao("sub", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                        .comentario("subtrai $t1 de $t0");
                gerador.instrucao("sltiu", RegMIPS32.T0, RegMIPS32.T0, 1)
                        .comentario("define $t0 como 1 se era 0");
            }
            case DIFERENTE -> {
                gerador.instrucao("sub", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                        .comentario("subtrai $t1 de $t0");
                gerador.instrucao("sltu", RegMIPS32.T0, RegMIPS32.ZERO, RegMIPS32.T0)
                        .comentario("define $t0 como 1 se diferentes");
            }
            case MENOR -> {
                gerador.instrucao("slt", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                        .comentario("verifica se $t0 < $t1");
            }
            case MAIOR -> {
                gerador.instrucao("slt", RegMIPS32.T0, RegMIPS32.T1, RegMIPS32.T0)
                        .comentario("verifica se $t1 < $t0");
            }
            case MENOR_IGUAL -> {
                gerador.instrucao("slt", RegMIPS32.T0, RegMIPS32.T1, RegMIPS32.T0)
                        .comentario("verifica se $t1 < $t0");
                gerador.instrucao("xori", RegMIPS32.T0, RegMIPS32.T0, 1)
                        .comentario("inverte para menor ou igual");
            }
            case MAIOR_IGUAL -> {
                gerador.instrucao("slt", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.T1)
                        .comentario("verifica se $t0 < $t1");
                gerador.instrucao("xori", RegMIPS32.T0, RegMIPS32.T0, 1)
                        .comentario("inverte para maior ou igual");
            }
            default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
        }
    }

    private void guardaComparacaoFloatEmT0(boolean inverter) {
        String labelFalso = novoLabel("comparacaoFalso");
        String labelFim = novoLabel("comparacaoFim");

        gerador.instrucao("bc1f", labelFalso);
        gerador.instrucao("li", RegMIPS32.T0, inverter ? 0 : 1);
        gerador.instrucao("j", labelFim);
        gerador.label(labelFalso);
        gerador.instrucao("li", RegMIPS32.T0, inverter ? 1 : 0);
        gerador.label(labelFim);
    }

    // Avalia com curto-circuito: se o lado esquerdo já decide o resultado (falso para 'e', verdadeiro para 'ou'),
    // o lado direito não é avaliado. O resultado é sempre normalizado para 0 ou 1
    private void gerarLogica(ExpressaoBinaria expressao, String instrucaoCurtoCircuito) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        String labelCurtoCircuito = novoLabel("logicoCurto");

        gerador.comentario(nomeOperacao);

        gerarExpressao(expressao.getEsquerda());
        desempilhar(RegMIPS32.T0);
        gerador.instrucao(instrucaoCurtoCircuito, RegMIPS32.T0, labelCurtoCircuito)
                .comentario("curto-circuito");

        gerarExpressao(expressao.getDireita());
        desempilhar(RegMIPS32.T0);

        gerador.label(labelCurtoCircuito);
        gerador.instrucao("sltu", RegMIPS32.T0, RegMIPS32.ZERO, RegMIPS32.T0)
                .comentario("normaliza o resultado para 0 ou 1");
        empilhar(RegMIPS32.T0);

        gerador.comentario("fim " + nomeOperacao);
    }

    private void gerarUnaria(ExpressaoUnaria expressao) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        TipoVariavel tipo = expressao.getExpressao().getTipo();
        RegMIPS32 registrador = tipo.isFlutuante() ? RegMIPS32.F0
                : RegMIPS32.T0;

        gerador.comentario("inicio " + nomeOperacao);

        gerarExpressao(expressao.getExpressao());
        desempilhar(registrador);

        switch (expressao.getOperador()) {
            case NEGACAO -> {
                gerador.instrucao("seq", RegMIPS32.T0, RegMIPS32.T0, RegMIPS32.ZERO)
                        .comentario("inverte o valor booleano");
            }
            case NEGATIVO -> {
                if (tipo.isFlutuante()) {
                    gerador.instrucao("neg.s", RegMIPS32.F0, RegMIPS32.F0)
                            .comentario("inverte o sinal do número");
                } else {
                    gerador.instrucao("sub", RegMIPS32.T0, RegMIPS32.ZERO, RegMIPS32.T0)
                            .comentario("inverte o sinal do número");
                }
            }
        }

        empilhar(registrador);

        gerador.comentario("fim " + nomeOperacao);
    }

    private void gerarTernaria(ExpressaoTernaria expressao) {
        gerador.comentario("inicio expressão ternária");

        gerarExpressao(expressao.getCondicao());
        desempilharEmS0();

        String labelFalso = novoLabel("ternarioFalso");
        String labelFim = novoLabel("ternarioFim");

        gerador.instrucao("beqz", RegMIPS32.S0, labelFalso);

        gerarExpressao(expressao.getSe());
        gerador.instrucao("j", labelFim);

        gerador.label(labelFalso);
        gerarExpressao(expressao.getSenao());

        gerador.label(labelFim);
        gerador.comentario("fim expressão ternária");
    }

    // Atribui à variável alvo o valor empilhado por gerarValor. O valor continua na pilha, pois a atribuição também é
    // uma expressão: quem usa o resultado (outra expressão, ou o comando que a contém) é que o desempilha
    private void gerarAtribuicao(ExpressaoIdentificador alvo, Runnable gerarValor) {
        SimboloVariavel simbolo = alvo.getSimbolo();
        TipoVariavel tipo = simbolo.getTipo();
        String nome = simbolo.getNome();

        // O valor é avaliado antes do índice
        gerarValor.run();

        if (simbolo.isVetor()) {
            // O índice é empilhado e desempilhado aqui dentro, então ao final o valor volta a ser o topo da pilha
            calculaEnderecoElemento(alvo);
            gerador.instrucao("lw", RegMIPS32.T3, new Deslocamento(RegMIPS32.SP, 0))
                    .comentario("lê o valor a ser armazenado");
            gerador.instrucao(
                    instrucaoEscrita(tipo), RegMIPS32.T3, new Deslocamento(RegMIPS32.T0, 0)
            )
                    .comentario("armazena em " + nome + "[índice]");
        } else {
            gerador.instrucao("lw", RegMIPS32.T3, new Deslocamento(RegMIPS32.SP, 0))
                    .comentario("lê o valor a ser armazenado");
            gerador.instrucao(instrucaoEscrita(tipo), RegMIPS32.T3, enderecoEscalar(simbolo))
                    .comentario("armazena " + nome);
        }
    }

    // Par de instrucaoLeitura: grava 1 byte para car e uma palavra para os demais tipos
    private String instrucaoEscrita(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, FLUTUANTE, STRING -> "sw";
            case CARACTERE -> "sb";
            case VAZIO -> throw new BugCompilador("Escrita não suportada para " + tipo);
        };
    }

    // Coloca no registrador o endereço do primeiro elemento do vetor:
    // - global: os elementos ficam no segmento de dados, no label da variável
    // - local: os elementos ficam no próprio frame, então o endereço é calculado a partir do $fp
    // - parâmetro: o espaço do parâmetro guarda o endereço que quem chamou passou, então ele é lido de lá
    private void carregaEnderecoVetor(SimboloVariavel vetor, RegMIPS32 registrador) {
        GeradorDeCodigoMIPS32.AjusteInstrucao instrucao = switch (vetor) {
            case SimboloVariavelGlobal global ->
                gerador.instrucao("la", registrador, labelVariavelGlobal(global.getNome()));
            case SimboloVariavelLocal local ->
                gerador.instrucao("addiu", registrador, RegMIPS32.FP, frameAtual.offset(local));
            case SimboloParametro parametro ->
                gerador.instrucao(
                        "lw", registrador, new Deslocamento(RegMIPS32.FP, frameAtual.offset(parametro))
                );
        };
        instrucao.comentario("endereço do vetor " + vetor.getNome());
    }

    private void gerarEscreva(ComandoEscreva comandoEscreva) {
        TipoVariavel tipo = comandoEscreva.getExpressao().getTipo();
        gerarExpressao(comandoEscreva.getExpressao());

        gerador.comentario("inicio escreva");

        switch (tipo) {
            case INTEIRO -> gerador.instrucao("li", RegMIPS32.V0, 1);
            case CARACTERE -> gerador.instrucao("li", RegMIPS32.V0, 11);
            case STRING -> gerador.instrucao("li", RegMIPS32.V0, 4);
            case FLUTUANTE -> gerador.instrucao("li", RegMIPS32.V0, 2);
            case VAZIO -> throw new BugCompilador("Escrita de valor vazio passou pela análise semântica");
        }

        // Carregar do topo do stack
        if (tipo.isFlutuante()) {
            gerador.instrucao("lwc1", RegMIPS32.F12, new Deslocamento(RegMIPS32.SP, 0));
        } else {
            gerador.instrucao("lw", RegMIPS32.A0, new Deslocamento(RegMIPS32.SP, 0));
        }
        gerador.instrucao("syscall").comentario("fim escreva");

        desempilharEmS0();
    }

    // Ler um valor é atribuí-lo à variável: o valor lido é empilhado e então armazenado
    private void gerarLeia(ComandoLeia comandoLeia) {
        gerador.comentario("inicio comando leia");

        ExpressaoIdentificador alvo = comandoLeia.getExpressaoIdentificador();
        gerarAtribuicao(alvo, () -> gerarLeitura(alvo.getSimbolo().getTipo()));

        // Como comando, o valor que a atribuição deixou na pilha não é usado
        desempilharEmS0();

        gerador.comentario("fim comando leia");
    }

    // Lê um valor da entrada padrão e o empilha
    private void gerarLeitura(TipoVariavel tipo) {
        int syscall = switch (tipo) {
            case INTEIRO -> 5;
            case CARACTERE -> 12;
            case FLUTUANTE -> 6;
            default -> throw new BugCompilador("Leitura de " + tipo + " passou pela análise semântica");
        };
        gerador.instrucao("li", RegMIPS32.V0, syscall).comentario("syscall para ler " + tipo);
        gerador.instrucao("syscall");
        RegMIPS32 registrador = tipo.isFlutuante() ? RegMIPS32.F0
                : RegMIPS32.V0;

        empilhar(registrador);
    }

    private void gerarChamada(ExpressaoChamadaFuncao chamada) {
        switch (chamada.getFuncao()) {
            case SimboloFuncaoNativa nativa -> gerarChamadaNativa(chamada, nativa.funcao());
            case SimboloFuncao funcao -> gerarChamadaFuncao(chamada, funcao.declaracao());
        }
    }

    // Cada função nativa é implementada do jeito que for mais simples para o MIPS:
    // - intrínseca: o código é gerado no lugar da chamada, sem jal
    // - rotina de biblioteca: o argumento vai em $a0 e o resultado volta em $v0
    private void gerarChamadaNativa(ExpressaoChamadaFuncao chamada, FuncaoNativa funcao) {
        for (Expressao argumento : chamada.getArgumentos()) {
            gerarExpressao(argumento);
        }

        switch (funcao) {
            case PISO -> {
                // Uma instrução de FPU resolve: não vale o custo de uma chamada
                gerador.comentario("piso (intrínseca)");
                desempilhar(RegMIPS32.F0);
                gerador.instrucao("floor.w.s", RegMIPS32.F0, RegMIPS32.F0)
                        .comentario("arredonda para baixo, resultado inteiro em $f0");
                gerador.instrucao("mfc1", RegMIPS32.T0, RegMIPS32.F0)
                        .comentario("move o inteiro para $t0");
                empilhar(RegMIPS32.T0);
                gerador.comentario("fim piso");
            }
            case RAND -> {
                gerador.comentario("rand (rotina de biblioteca)");
                gerador.incluirRotina(RotinaMIPS32.RAND);
                desempilhar(RegMIPS32.A0);
                gerador.instrucao("jal", RotinaMIPS32.RAND.label);
                empilhar(RegMIPS32.V0);
                gerador.comentario("fim rand");
            }
            case RAIZ -> {
                gerador.comentario("raiz (intrínseca)");
                desempilhar(RegMIPS32.F0);
                gerador.instrucao("sqrt.s", RegMIPS32.F0, RegMIPS32.F0);
                empilhar(RegMIPS32.F0);
                gerador.comentario("fim raiz");
            }
            // Obriga o compilador a exigir um case para cada FuncaoNativa: sem ele, um switch de enum usado como
            // comando aceita casos faltando, e uma nativa nova compilaria sem gerar código
            case null -> throw new BugCompilador("Chamada de função nativa sem a função");
        }
    }

    // Convenção de chamada: quem chama empilha os argumentos em ordem, e a função chamada salva $ra e $fp no prólogo.
    // A função chamada limpa tudo isso antes de retornar, deixando o resultado em $v0
    private void gerarChamadaFuncao(ExpressaoChamadaFuncao chamada, DeclaracaoFuncao funcao) {
        String nomeFuncao = funcao.getNome();
        List<Expressao> argumentos = chamada.getArgumentos();

        gerador.comentario("inicio chamada da função " + nomeFuncao);

        for (int i = 0; i < argumentos.size(); i++) {
            if (funcao.getParametros().get(i).isVetor()) {
                empilharEnderecoVetor(argumentos.get(i));
            } else {
                gerarExpressao(argumentos.get(i));
            }
        }

        gerador.instrucao("jal", labelFuncao(nomeFuncao)).comentario("chama a função " + nomeFuncao);
        empilhar(RegMIPS32.V0); // armazena o resultado da função no stack

        gerador.comentario("fim chamada da função " + nomeFuncao);
    }

    // Empilha o endereço do primeiro elemento do vetor, que é como vetores são passados para funções.
    // A análise semântica garante que o argumento é o nome de um vetor, possivelmente entre parênteses
    private void empilharEnderecoVetor(Expressao argumento) {
        if (!(argumento instanceof ExpressaoIdentificador identificador)) {
            throw new BugCompilador("Argumento vetor não é um identificador: " + argumento.codigoOriginal());
        }

        SimboloVariavel simbolo = identificador.getSimbolo();
        carregaEnderecoVetor(simbolo, RegMIPS32.S0);
        empilharS0();
    }

    private void gerarRetorno(ComandoRetorno comandoRetorno) {
        gerador.comentario("inicio comando retorno");

        Expressao expressao = comandoRetorno.getExpressao();
        gerarExpressao(expressao);

        if (funcaoAtual == null) {
            // No bloco programa, o valor retornado é o código de saída
            if (expressao.getTipo() != TipoVariavel.VAZIO) {
                desempilhar(RegMIPS32.A0);
            } else {
                gerador.instrucao("li", RegMIPS32.A0, 0).comentario("retorne sem valor sai com código 0");
            }

            gerador.instrucao("li", RegMIPS32.V0, 17)
                    .comentario("syscall para finalizar o programa com código de retorno");
            gerador.instrucao("syscall").comentario("fim main");
        } else {
            if (!funcaoAtual.getTipoRetorno().isTipoVazio()) {
                desempilhar(RegMIPS32.V0); // Carrega o valor de retorno em $v0
            }
            finalizarFuncao(funcaoAtual.getNome());
        }

        gerador.comentario("fim comando retorno");
    }

    private void finalizarFuncao(String nomeFuncao) {
        if (funcaoAtual == null) {
            throw new BugCompilador("Deveríamos estar dentro da função " + nomeFuncao);
        }
        int n = funcaoAtual.getParametros().size();
        // Epílogo da função: restaura o $ra, descarta de uma vez frame, $ra e $fp salvos e argumentos, restaura o $fp e
        // retorna. Como o $fp não muda durante a função, serve para um retorne em qualquer profundidade de blocos.
        // O $fp antigo é lido depois de o $sp subir: liberar não apaga a memória, e no MARS nada escreve abaixo do $sp
        // entre as duas instruções. Num sistema com signal handlers isso não vale, e as leituras teriam que vir antes
        // do addiu
        gerador.instrucao("lw", RegMIPS32.RA, new Deslocamento(RegMIPS32.FP, 4))
                .comentario("restaura o endereço de retorno");
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.FP, 8 + 4 * n)
                .comentario("descarta frame, $ra e $fp salvos e argumentos de uma vez");
        gerador.instrucao("lw", RegMIPS32.FP, new Deslocamento(RegMIPS32.FP, 0))
                .comentario("restaura o $fp");
        gerador.instrucao("jr", RegMIPS32.RA).comentario("retornando da função " + nomeFuncao);
    }

    private void gerarSe(ComandoSe comandoSe) {
        String labelFalso = novoLabel("seFalso");
        String labelFim = novoLabel("seFim");

        gerador.comentario("inicio comando se");

        // Avalia a expressão condicional
        gerarExpressao(comandoSe.getSe());
        desempilhar(RegMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFalso se for 0 (falso)
        gerador.instrucao("beqz", RegMIPS32.T0, labelFalso);

        gerarComando(comandoSe.getConsequencia());

        // Pula para o final se um bloco alternativo existir
        if (comandoSe.getAlternativa() != null) {
            gerador.instrucao("j", labelFim);
        }

        // Label para o bloco alternativo/final se não houver senao
        gerador.label(labelFalso);
        if (comandoSe.getAlternativa() != null) {
            gerarComando(comandoSe.getAlternativa());
            gerador.label(labelFim);
        }

        gerador.comentario("fim comando se");
    }

    private void gerarEnquanto(ComandoEnquanto comandoEnquanto) {
        String labelInicio = novoLabel("enquantoInicio");
        String labelPasso = comandoEnquanto.getPasso() == null ? labelInicio : novoLabel("enquantoPasso");
        String labelFim = novoLabel("enquantoFim");
        labelsDosLacos.put(comandoEnquanto, new LabelsPulo(labelPasso, labelFim));

        gerador.comentario("inicio comando enquanto");
        gerador.label(labelInicio);

        // Avalia a expressão condicional
        gerarExpressao(comandoEnquanto.getCondicional());
        desempilhar(RegMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFim se for 0 (falso)
        gerador.instrucao("beqz", RegMIPS32.T0, labelFim);

        gerarComando(comandoEnquanto.getComando());
        if (comandoEnquanto.getPasso() != null) {
            gerador.label(labelPasso);
            gerarComando(comandoEnquanto.getPasso());
        }

        // Retorna ao início para avaliar a condição novamente
        gerador.instrucao("j", labelInicio);

        gerador.label(labelFim);
        gerador.comentario("fim comando enquanto");
    }

    private void gerarContinue(ComandoContinue comando) {
        LabelsPulo labels = labelsDosLacos.get(comando.getEnquanto());
        if (labels == null) {
            throw new BugCompilador("Comando continue sem laço associado");
        }
        gerador.instrucao("j", labels.continueLabel()).comentario("continue");
    }

    private void gerarPare(ComandoPare comando) {
        LabelsPulo labels = labelsDosLacos.get(comando.getEnquanto());
        if (labels == null) {
            throw new BugCompilador("Comando pare sem laço associado");
        }
        gerador.instrucao("j", labels.fimLabel()).comentario("pare");
    }

    private record LabelsPulo(String continueLabel, String fimLabel) {
    }

    private void empilharS0() {
        empilhar(RegMIPS32.S0);
    }

    // Empilha o registrador numa palavra nova no topo da pilha
    private void empilhar(RegMIPS32 registrador) {
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -4);

        String store = registrador.isPontoFlutuante() ? "swc1" : "sw";
        gerador.instrucao(store, registrador, new Deslocamento(RegMIPS32.SP, 0))
                .comentario("empilha " + registrador);
    }

    private void desempilharEmS0() {
        desempilhar(RegMIPS32.S0);
    }

    // Tira a palavra do topo da pilha e a coloca no registrador
    private void desempilhar(RegMIPS32 registrador) {
        String load = registrador.isPontoFlutuante() ? "lwc1" : "lw";
        gerador.instrucao(load, registrador, new Deslocamento(RegMIPS32.SP, 0))
                .comentario("desempilha em " + registrador);
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, 4);
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
