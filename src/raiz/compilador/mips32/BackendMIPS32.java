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
// - Parâmetros e variáveis locais são acessados em relação ao $fp, que fica fixo durante toda a função (ver FrameMIPS32).
//   O $sp só se move para os valores temporários das expressões
// - $s0 e $t0-$t3 são de rascunho: não guardam nada entre um comando e outro. $a0-$a2 levam argumentos para as
//   rotinas de runtime e funções nativas, e $v0 traz o resultado de funções
public class BackendMIPS32 implements Backend {

    private final GeradorDeCodigoMIPS32 gerador = new GeradorDeCodigoMIPS32();
    private final Map<String, String> stringsParaLabels = new HashMap<>();
    private int contadorLabels = 0;

    // Layout do frame da função (ou do bloco programa) cujo código está sendo gerado
    private FrameMIPS32 frameAtual;

    // Função cujo código está sendo gerado; null no bloco programa
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
                        gerador.gerar(
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

        gerador.gerar(labelFuncao(funcao.getNome()) + ":");

        // Os argumentos já foram empilhados por quem chamou a função
        frameAtual = FrameMIPS32.deFuncao(funcao);
        // Prólogo: salva $ra e o $fp de quem chamou, aponta o $fp para eles e reserva o espaço das variáveis locais
        gerador.gerar("addiu $sp, $sp, -8 # espaço para o $ra e o $fp antigo");
        gerador.gerar("sw    $ra, 4($sp)  # guarda endereço de retorno");
        gerador.gerar("sw    $fp, 0($sp)  # guarda o $fp de quem chamou");
        gerador.gerar("move  $fp, $sp     # o frame da função começa aqui");
        reservaFrame();

        funcaoAtual = funcao;
        gerarBloco(funcao.getCorpo());

        // Se a função é de retorno vazio, temos que finalizá-la
        if (funcao.getTipoRetorno().isTipoVazio()) {
            finalizarFuncao(funcao.getNome());
        }

        gerador.gerar("# fim função " + funcao.getNome() + "\n");
    }

    private void gerarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        frameAtual = FrameMIPS32.dePrograma(blocoPrograma);

        gerador.gerar(".globl main");
        gerador.gerar("main:");
        gerador.gerar("move  $fp, $sp # o frame do programa começa aqui");
        reservaFrame();

        funcaoAtual = null;
        gerarBloco(blocoPrograma.getBlocoDeclaracoes());

        // Chegar ao fim do bloco programa encerra o programa com código de saída 0
        gerador.gerar("li    $v0, 10 # syscall para encerrar o programa");
        gerador.gerar("syscall       # fim main");
    }

    // Reserva de uma vez o espaço de todas as variáveis locais do frame atual. Blocos não reservam nem liberam espaço
    private void reservaFrame() {
        int tamanho = frameAtual.getTamanho();
        if (tamanho > 0) {
            gerador.gerar("addiu $sp, $sp, -" + tamanho + " # reserva o frame");
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

    // Chama a rotina de runtime com o intervalo [início, fim) do vetor no frame. O tamanho é múltiplo de 4, então a
    // rotina zera palavra por palavra. Sobrescrever o $ra com o jal não tem problema: o prólogo já o salvou
    private void zerarVetor(Variavel variavel) {
        int inicio = frameAtual.offset(variavel);
        int fim = inicio + FrameMIPS32.tamanhoVariavel(variavel.getSimbolo());

        gerador.incluirRotina(RotinaMIPS32.ZERA_VETOR);
        gerador.gerar("addiu $a0, $fp, " + inicio + " # início do vetor " + variavel.getNome());
        gerador.gerar("addiu $a1, $fp, " + fim + " # fim do vetor " + variavel.getNome());
        gerador.gerar("jal   " + RotinaMIPS32.ZERA_VETOR.label + " # zera o vetor " + variavel.getNome());
    }

    private void gerarComando(Comando comando) {
        switch (comando) {
            case ComandoBloco comandoBloco -> gerarBloco(comandoBloco.getDeclaracoes());
            case ComandoEscreva comandoEscreva -> gerarEscreva(comandoEscreva);
            case ComandoLeia comandoLeia -> gerarLeia(comandoLeia);
            case ComandoNovalinha ignored -> gerarNovalinha();
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
            case ExpressaoEntreParenteses entreParenteses -> gerarExpressao(entreParenteses.getExpressao());
            case ExpressaoIdentificador identificador -> gerarIdentificador(identificador);
            case ExpressaoInteiroLiteral inteiro -> {
                int valor = inteiro.getConteudo();
                gerador.gerar("li    $s0, " + valor + " # empilhando inteiro literal " + valor);
                empilharS0();
            }
            case ExpressaoFlutuanteLiteral flutuante -> {
                float valor = flutuante.getConteudo();
                // Carrega o valor como um int no padrão IEEE 754
                gerador.gerar("li    $s0, " + Float.floatToRawIntBits(valor) + " # empilhando flutuante literal " + valor);
                empilharS0();
            }
            case ExpressaoCaractereLiteral caractere -> {
                char valor = caractere.getConteudo();
                gerador.gerar("li    $s0, " + (int) valor + " # empilhando caractere literal " + valor);
                empilharS0();
            }
            case ExpressaoStringLiteral string -> gerarString(string);
            case ExpressaoAtribuicao atribuicao -> gerarAtribuicao(
                    atribuicao.getIdentificador(),
                    () -> gerarExpressao(atribuicao.getExpressaoLadoDireito())
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
    private String enderecoEscalar(SimboloVariavel simbolo) {
        return switch (simbolo) {
            case SimboloVariavelGlobal global -> labelVariavelGlobal(global.getNome());
            case SimboloVariavelLocal local -> frameAtual.offset(local) + "($fp)";
            case SimboloParametro parametro -> frameAtual.offset(parametro) + "($fp)";
        };
    }

    // Avalia o índice e deixa em $t0 o endereço do elemento: base + índice * tamanho do elemento.
    // Toda leitura e escrita de elemento de vetor passa por aqui, então é aqui que a checagem de limites da #20 entra
    private void calculaEnderecoElemento(ExpressaoIdentificador vetor) {
        SimboloVariavel simbolo = vetor.getSimbolo();
        gerarExpressao(vetor.getIndex());
        desempilharEmS0();
        // A base é carregada depois do índice: uma chamada de função dentro do índice estragaria o $t0
        carregaEnderecoVetor(simbolo, RegistradoresMIPS32.T0);
        // Elementos de 1 byte (car) não precisam de multiplicação
        if (FrameMIPS32.getEspacoMemoria(simbolo.getTipo()) == 4) {
            gerador.gerar("sll   $s0, $s0, 2 # índice * 4 bytes");
        }
        gerador.gerar("addu  $t0, $t0, $s0 # endereço de " + simbolo.getNome() + "[índice]");
    }

    // Empilha o valor de uma variável ou de um elemento de vetor
    private void gerarIdentificador(ExpressaoIdentificador identificador) {
        SimboloVariavel simbolo = identificador.getSimbolo();
        String endereco = "0($t0)";
        if (simbolo.isVetor()) {
            calculaEnderecoElemento(identificador);
        } else {
            endereco = enderecoEscalar(simbolo);
        }
        gerador.gerar("%-6s$s0, %s # lendo %s".formatted(instrucaoLeitura(simbolo.getTipo()), endereco, simbolo.getNome()));
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
        gerador.gerar("la    $s0, " + label + " # empilhando endereço de string literal");
        empilharS0();
    }

    private void gerarNovalinha() {
        gerador.gerar("li    $v0, 11 # novaLinha"); // Syscall para imprimir char
        gerador.gerar("li    $a0, 10");             // ASCII do \n
        gerador.gerar("syscall       # fim novaLinha");
    }

    private void gerarBinaria(ExpressaoBinaria expressao) {
        OperadorBinario operador = expressao.getOperador();

        if (operador.getCategoria() == OperadorBinario.Categoria.LOGICA) {
            gerarLogica(expressao, operador == OperadorBinario.E ? "beqz" : "bnez");
            return;
        }

        String nomeOperacao = operador.getDescricao();
        gerador.gerar("# " + nomeOperacao);

        TipoVariavel tipoEsquerdo = expressao.getEsquerda().getTipo();
        TipoVariavel tipoDireito = expressao.getDireita().getTipo();
        gerarExpressao(expressao.getEsquerda());
        gerarExpressao(expressao.getDireita());

        // Lê o segundo valor em $t1/$f1 e o primeiro em $t0/$f0
        desempilhar(tipoDireito == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F1 : RegistradoresMIPS32.T1);
        desempilhar(tipoEsquerdo == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0);

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
        empilhar(expressao.getTipo() == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);
    }

    // Usa os valores em $t0/$f0 (esquerdo) e $t1/$f1 (direito) e coloca o resultado em $t0/$f0
    private void gerarOperacaoAritmetica(ExpressaoBinaria expressao, TipoVariavel tipo) {
        OperadorBinario operador = expressao.getOperador();
        boolean flutuante = tipo == TipoVariavel.FLUTUANTE;

        switch (operador) {
            case SOMA -> gerador.gerar(
                    flutuante
                            ? "add.s $f0, $f0, $f1 # soma $f0 e $f1, resultado em $f0"
                            : "add   $t0, $t0, $t1 # soma $t0 e $t1, resultado em $t0"
            );
            case SUBTRACAO -> gerador.gerar(
                    flutuante
                            ? "sub.s $f0, $f0, $f1 # subtrai $f0 e $f1, resultado em $f0"
                            : "sub   $t0, $t0, $t1 # subtrai $t1 de $t0, resultado em $t0"
            );
            case MULTIPLICACAO -> gerador.gerar(
                    flutuante
                            ? "mul.s $f0, $f0, $f1 # multiplica $f0 e $f1, resultado em $f0"
                            : "mul   $t0, $t0, $t1 # multiplica $t0 por $t1, resultado em $t0"
            );
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

        gerador.incluirRotina(RotinaMIPS32.ERRO_EXECUCAO);
        gerador.gerar("la    $a1, " + labelMensagem);
        gerador.gerar("li    $a2, " + bytes.length);
        gerador.gerar("j     " + RotinaMIPS32.ERRO_EXECUCAO.label);
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
    private void gerarLogica(ExpressaoBinaria expressao, String instrucaoCurtoCircuito) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        String labelCurtoCircuito = novoLabel("logicoCurto");

        gerador.gerar("# " + nomeOperacao);

        gerarExpressao(expressao.getEsquerda());
        desempilhar(RegistradoresMIPS32.T0);
        gerador.gerar(instrucaoCurtoCircuito + "  $t0, " + labelCurtoCircuito + " # curto-circuito");

        gerarExpressao(expressao.getDireita());
        desempilhar(RegistradoresMIPS32.T0);

        gerador.gerar(labelCurtoCircuito + ":");
        gerador.gerar("sltu  $t0, $zero, $t0 # normaliza o resultado para 0 ou 1");
        empilhar(RegistradoresMIPS32.T0);

        gerador.gerar("# fim " + nomeOperacao);
    }

    private void gerarUnaria(ExpressaoUnaria expressao) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        TipoVariavel tipo = expressao.getExpressao().getTipo();
        RegistradoresMIPS32 registrador = tipo == TipoVariavel.FLUTUANTE ? RegistradoresMIPS32.F0 : RegistradoresMIPS32.T0;

        gerador.gerar("# inicio " + nomeOperacao);

        gerarExpressao(expressao.getExpressao());
        desempilhar(registrador);

        switch (expressao.getOperador()) {
            case NEGACAO -> gerador.gerar("seq   $t0, $t0, $zero # inverte o valor booleano, 1 se $t0 é 0, 0 caso contrário");
            case NEGATIVO -> gerador.gerar(
                    tipo == TipoVariavel.FLUTUANTE
                            ? "neg.s $f0, $f0 # inverte o sinal do número"
                            : "sub   $t0, $zero, $t0 # inverte o sinal do número"
            );
        }

        empilhar(registrador);

        gerador.gerar("# fim " + nomeOperacao);
    }

    private void gerarTernaria(ExpressaoTernaria expressao) {
        gerador.gerar("# inicio expressão ternária");

        gerarExpressao(expressao.getCondicao());
        desempilharEmS0();

        String labelFalso = novoLabel("ternarioFalso");
        String labelFim = novoLabel("ternarioFim");

        gerador.gerar("beqz  $s0, " + labelFalso);

        gerarExpressao(expressao.getSe());
        gerador.gerar("j     " + labelFim);

        gerador.gerar(labelFalso + ":");
        gerarExpressao(expressao.getSenao());

        gerador.gerar(labelFim + ": # fim expressão ternária");
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
            gerador.gerar("lw    $t3, 0($sp) # lê o valor a ser armazenado");
            gerador.gerar("%-6s$t3, 0($t0) # armazena em %s[índice]".formatted(instrucaoEscrita(tipo), nome));
        } else {
            gerador.gerar("lw    $t3, 0($sp) # lê o valor a ser armazenado");
            gerador.gerar(
                    "%-6s$t3, %s # armazena %s".formatted(instrucaoEscrita(tipo), enderecoEscalar(simbolo), nome)
            );
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
    private void carregaEnderecoVetor(SimboloVariavel vetor, RegistradoresMIPS32 registrador) {
        String destino = registrador.getNome();
        String comentario = " # endereço do vetor " + vetor.getNome();
        switch (vetor) {
            case SimboloVariavelGlobal global ->
                gerador.gerar("la    " + destino + ", " + labelVariavelGlobal(global.getNome()) + comentario);
            case SimboloVariavelLocal local -> gerador.gerar("addiu " + destino + ", $fp, " + frameAtual.offset(local) + comentario);
            case SimboloParametro parametro ->
                gerador.gerar("lw    " + destino + ", " + frameAtual.offset(parametro) + "($fp)" + comentario);
        }
    }

    private void gerarEscreva(ComandoEscreva comandoEscreva) {
        TipoVariavel tipo = comandoEscreva.getExpressao().getTipo();
        gerarExpressao(comandoEscreva.getExpressao());

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

        desempilharEmS0();
    }

    // Ler um valor é atribuí-lo à variável: o valor lido é empilhado e então armazenado
    private void gerarLeia(ComandoLeia comandoLeia) {
        gerador.gerar("# inicio comando leia");

        ExpressaoIdentificador alvo = comandoLeia.getExpressaoIdentificador();
        gerarAtribuicao(alvo, () -> gerarLeitura(alvo.getSimbolo().getTipo()));

        // Como comando, o valor que a atribuição deixou na pilha não é usado
        desempilharEmS0();

        gerador.gerar("# fim comando leia");
    }

    // Lê um valor da entrada padrão e o empilha
    private void gerarLeitura(TipoVariavel tipo) {
        switch (tipo) {
            case INTEIRO -> gerador.gerar("li    $v0, 5 # syscall para ler inteiro");
            case CARACTERE -> gerador.gerar("li    $v0, 12 # syscall para ler caractere");
            default -> throw new BugCompilador("Leitura de " + tipo + " passou pela análise semântica");
        }
        gerador.gerar("syscall");

        empilhar(RegistradoresMIPS32.V0);
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
                gerador.gerar("# piso (intrínseca)");
                desempilhar(RegistradoresMIPS32.F0);
                gerador.gerar("floor.w.s $f0, $f0 # arredonda para baixo, resultado inteiro em $f0");
                gerador.gerar("mfc1  $t0, $f0     # move o inteiro para $t0");
                empilhar(RegistradoresMIPS32.T0);
                gerador.gerar("# fim piso");
            }
            case RAND -> {
                gerador.gerar("# rand (rotina de biblioteca)");
                gerador.incluirRotina(RotinaMIPS32.RAND);
                desempilhar(RegistradoresMIPS32.A0);
                gerador.gerar("jal   " + RotinaMIPS32.RAND.label);
                empilhar(RegistradoresMIPS32.V0);
                gerador.gerar("# fim rand");
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

        gerador.gerar("# inicio chamada da função " + nomeFuncao);

        for (int i = 0; i < argumentos.size(); i++) {
            if (funcao.getParametros().get(i).isVetor()) {
                empilharEnderecoVetor(argumentos.get(i));
            } else {
                gerarExpressao(argumentos.get(i));
            }
        }

        gerador.gerar("jal   " + labelFuncao(nomeFuncao) + " # chama a função " + nomeFuncao);
        empilhar(RegistradoresMIPS32.V0); // armazena o resultado da função no stack

        gerador.gerar("# fim chamada da função " + nomeFuncao);
    }

    // Empilha o endereço do primeiro elemento do vetor, que é como vetores são passados para funções.
    // A análise semântica garante que o argumento é o nome de um vetor, possivelmente entre parênteses
    private void empilharEnderecoVetor(Expressao argumento) {
        while (argumento instanceof ExpressaoEntreParenteses entreParenteses) {
            argumento = entreParenteses.getExpressao();
        }
        if (!(argumento instanceof ExpressaoIdentificador identificador)) {
            throw new BugCompilador("Argumento vetor não é um identificador: " + argumento.codigoOriginal());
        }

        SimboloVariavel simbolo = identificador.getSimbolo();
        carregaEnderecoVetor(simbolo, RegistradoresMIPS32.S0);
        empilharS0();
    }

    private void gerarRetorno(ComandoRetorno comandoRetorno) {
        gerador.gerar("# inicio comando retorno");

        Expressao expressao = comandoRetorno.getExpressao();
        gerarExpressao(expressao);

        if (funcaoAtual == null) {
            // No bloco programa, o valor retornado é o código de saída
            if (expressao.getTipo() != TipoVariavel.VAZIO) {
                desempilhar(RegistradoresMIPS32.A0);
            } else {
                gerador.gerar("li    $a0, 0 # retorne sem valor sai com código 0");
            }

            gerador.gerar("li    $v0, 17 # syscall para finalizar o programa com código de retorno");
            gerador.gerar("syscall       # fim main");
        } else {
            if (!funcaoAtual.getTipoRetorno().isTipoVazio()) {
                desempilhar(RegistradoresMIPS32.V0); // Carrega o valor de retorno em $v0
            }
            finalizarFuncao(funcaoAtual.getNome());
        }

        gerador.gerar("# fim comando retorno");
    }

    private void finalizarFuncao(String nomeFuncao) {
        if (funcaoAtual == null) {
            throw new BugCompilador("Deveríamos estar dentro da função " + nomeFuncao);
        }
        int n = funcaoAtual.getParametros().size();
        // Epílogo da função: restaura o $ra, descarta de uma vez frame, $ra e $fp salvos e argumentos, restaura o $fp e retorna.
        // Como o $fp não muda durante a função, serve para um retorne em qualquer profundidade de blocos.
        // O $fp antigo é lido depois de o $sp subir: liberar não apaga a memória, e no MARS nada escreve abaixo do $sp entre
        // as duas instruções. Num sistema com signal handlers isso não vale, e as leituras teriam que vir antes do addiu
        gerador.gerar("lw    $ra, 4($fp) # restaura o endereço de retorno");
        gerador.gerar("addiu $sp, $fp, %d # descarta frame, $ra e $fp salvos e argumentos de uma vez".formatted(8 + 4 * n));
        gerador.gerar("lw    $fp, 0($fp)  # por último: é o último uso do $fp atual como base");
        gerador.gerar("jr    $ra         # retornando da função " + nomeFuncao);
    }

    private void gerarSe(ComandoSe comandoSe) {
        String labelFalso = novoLabel("seFalso");
        String labelFim = novoLabel("seFim");

        gerador.gerar("# inicio comando se");

        // Avalia a expressão condicional
        gerarExpressao(comandoSe.getSe());
        desempilhar(RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFalso se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFalso);

        gerarComando(comandoSe.getConsequencia());

        // Pula para o final se um bloco alternativo existir
        if (comandoSe.getAlternativa() != null) {
            gerador.gerar("j     " + labelFim);
        }

        // Label para o bloco alternativo/final se não houver senao
        gerador.gerar(labelFalso + ":");
        if (comandoSe.getAlternativa() != null) {
            gerarComando(comandoSe.getAlternativa());
            gerador.gerar(labelFim + ":");
        }

        gerador.gerar("# fim comando se");
    }

    private void gerarEnquanto(ComandoEnquanto comandoEnquanto) {
        String labelInicio = novoLabel("enquantoInicio");
        String labelFim = novoLabel("enquantoFim");

        gerador.gerar("# inicio comando enquanto");
        gerador.gerar(labelInicio + ":");

        // Avalia a expressão condicional
        gerarExpressao(comandoEnquanto.getCondicional());
        desempilhar(RegistradoresMIPS32.T0); // Carrega o resultado da condição em $t0

        // Verifica a condição e pula para labelFim se for 0 (falso)
        gerador.gerar("beqz  $t0, " + labelFim);

        gerarComando(comandoEnquanto.getComando());

        // Retorna ao início para avaliar a condição novamente
        gerador.gerar("j     " + labelInicio);

        gerador.gerar(labelFim + ":");
        gerador.gerar("# fim comando enquanto");
    }

    private void empilharS0() {
        empilhar(RegistradoresMIPS32.S0);
    }

    // Empilha o registrador numa palavra nova no topo da pilha
    private void empilhar(RegistradoresMIPS32 registrador) {
        gerador.gerar("addiu $sp, $sp, -4 # empilha " + registrador.getNome());
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("swc1  " + registrador.getNome() + ", 0($sp)");
        } else {
            gerador.gerar("sw    " + registrador.getNome() + ", 0($sp)");
        }
    }

    private void desempilharEmS0() {
        desempilhar(RegistradoresMIPS32.S0);
    }

    // Tira a palavra do topo da pilha e a coloca no registrador
    private void desempilhar(RegistradoresMIPS32 registrador) {
        if (registrador.isPontoFlutuante()) {
            gerador.gerar("lwc1  " + registrador.getNome() + ", 0($sp) # desempilha em " + registrador.getNome());
        } else {
            gerador.gerar("lw    " + registrador.getNome() + ", 0($sp) # desempilha em " + registrador.getNome());
        }
        gerador.gerar("addiu $sp, $sp, 4");
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
