package src.raiz.compilador.mips32;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.Backend;
import src.raiz.compilador.ModoGerador;
import src.raiz.compilador.mips32.AlocadorRegistradores.Preferencia;
import src.raiz.erros.BugCompilador;
import src.raiz.semantica.*;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

// Gera assembly MIPS32 (no dialeto do MARS) a partir de um programa anotado pelo AnalisadorSemantico.
//
// Convenções do código gerado:
// - Toda expressão é avaliada num Local (ver avaliar): normalmente um registrador entregue pelo
//   AlocadorRegistradores, ou um slot de spill quando eles acabam. Quem recebe o Local é dono dele e o libera
// - Parâmetros e variáveis locais são acessados em relação ao $fp (ver FrameMIPS32), que fica fixo durante toda a
//   função. Logo abaixo das variáveis locais ficam os slots de spill e os registradores salvos ($s, $f20-$f31) que a
//   função usa. O $sp só se move em volta das chamadas
// - $t8, $t9, $f18 e $f19 são de recarga: nunca guardam um temporário, só levam um valor entre a memória e uma
//   instrução. $a0-$a3 levam argumentos para as rotinas de runtime e funções nativas
// - Funções do usuário seguem a convenção o32 (ver AreaArgumentos): os argumentos dos primeiros 16 bytes vão em
//   $a0-$a3, ou em $f12/$f14 quando a função começa com flut, e o resultado volta em $v0, ou $f0 para flut. Quem
//   chama salva os temporários vivos ($t, $f4-$f11, $f16, $f17), e a função chamada salva os $s que usar
public class BackendMIPS32 implements Backend {

    private static final int MAX_PALAVRAS_ZERADAS_NO_LUGAR = 16;

    private final GeradorDeCodigoMIPS32 gerador = new GeradorDeCodigoMIPS32();
    private final Map<String, String> stringsParaLabels = new HashMap<>();
    private final Map<ComandoEnquanto, LabelsPulo> labelsDosLacos = new HashMap<>();
    private final Map<DeclaracaoFuncao, AreaArgumentos> areasArgumentos = new HashMap<>();
    private int contadorLabels = 0;

    // Layout do frame da função (ou do bloco programa) cujo código está sendo gerado
    private FrameMIPS32 frameAtual;

    // Os temporários da função (ou do bloco programa) cujo código está sendo gerado. Cada uma tem o seu
    private AlocadorRegistradores alocador;

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

    // O prólogo é inserido depois de gerado o corpo: o tamanho da área de spill e os registradores salvos que a
    // função usa só se sabem no fim
    private void gerarFuncao(DeclaracaoFuncao funcao) {
        gerador.setModoAtual(ModoGerador.FUNCAO);

        gerador.label(labelFuncao(funcao.getNome()));
        int prologo = gerador.pontoInsercao();

        frameAtual = FrameMIPS32.deFuncao(funcao);
        alocador = new AlocadorRegistradores(-frameAtual.getTamanho());
        funcaoAtual = funcao;

        gerarBloco(funcao.getCorpo());

        List<RegMIPS32> salvos = alocador.salvosUsados();
        gerador.inserirEm(prologo, () -> {
            // Salva $ra e o $fp de quem chamou, aponta o $fp para eles e reserva o frame
            gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -8)
                    .comentario("espaço para o $ra e o $fp antigo");
            gerador.instrucao("sw", RegMIPS32.RA, new Deslocamento(RegMIPS32.SP, 4))
                    .comentario("guarda endereço de retorno");
            gerador.instrucao("sw", RegMIPS32.FP, new Deslocamento(RegMIPS32.SP, 0))
                    .comentario("guarda o $fp de quem chamou");
            gerador.instrucao("move", RegMIPS32.FP, RegMIPS32.SP)
                    .comentario("o frame da função começa aqui");
            reservaFrame(salvos.size());
            for (int i = 0; i < salvos.size(); i++) {
                RegMIPS32 salvo = salvos.get(i);
                gerador.instrucao(armazenamento(salvo), salvo, enderecoSalvo(i))
                        .comentario("preserva " + salvo + " de quem chamou");
            }
            salvaArgumentosEmRegistrador(funcao);
        });

        finalizarFuncao(funcao.getNome(), salvos);

        gerador.comentario("fim função " + funcao.getNome());
        alocador = null;
    }

    private void gerarBlocoPrograma(BlocoPrograma blocoPrograma) {
        gerador.setModoAtual(ModoGerador.MAIN);

        gerador.diretiva(".globl main");
        gerador.label("main");
        int prologo = gerador.pontoInsercao();

        frameAtual = FrameMIPS32.dePrograma(blocoPrograma);
        alocador = new AlocadorRegistradores(-frameAtual.getTamanho());
        funcaoAtual = null;

        gerarBloco(blocoPrograma.getBlocoDeclaracoes());

        // Chegar ao fim do bloco programa encerra o programa com código de saída 0
        gerador.instrucao("li", RegMIPS32.V0, 10).comentario("syscall para encerrar o programa");
        gerador.instrucao("syscall").comentario("fim main");

        // O programa não volta para ninguém, então não precisa preservar os $s
        gerador.inserirEm(prologo, () -> {
            gerador.instrucao("move", RegMIPS32.FP, RegMIPS32.SP)
                    .comentario("o frame do programa começa aqui");
            reservaFrame(0);
        });
        alocador = null;
    }

    // Reserva de uma vez o frame: as variáveis locais, a área de spill e os registradores salvos. Blocos não
    // reservam nem liberam espaço
    private void reservaFrame(int quantidadeSalvos) {
        int tamanho = frameAtual.getTamanho() + alocador.tamanhoSpill()
                + quantidadeSalvos * FrameMIPS32.TAMANHO_PALAVRA;
        if (tamanho > 0) {
            gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -tamanho)
                    .comentario("reserva o frame");
        }
    }

    // Onde o i-ésimo registrador salvo da função fica guardado: logo abaixo da área de spill
    private Deslocamento enderecoSalvo(int i) {
        int inicio = frameAtual.getTamanho() + alocador.tamanhoSpill();
        return new Deslocamento(RegMIPS32.FP, -(inicio + FrameMIPS32.TAMANHO_PALAVRA * (i + 1)));
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
    // Sobrescrever o $ra com o jal não tem problema: o prólogo já o salvou. A rotina só usa $a0 e $a1, e entre dois
    // comandos não há temporário vivo
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
                // Ex: uma atribuição ou chamada usada como comando. O valor da expressão não é usado
                Expressao expressao = comandoComExpressao.getExpressao();
                if (expressao instanceof ExpressaoChamadaFuncao chamada) {
                    Local resultado = gerarChamada(chamada);
                    if (resultado != null) {
                        alocador.liberar(resultado);
                    }
                } else if (expressao.getTipo() != TipoVariavel.VAZIO) {
                    alocador.liberar(avaliar(expressao));
                }
            }
        }
        alocador.validarTodosLiberados();
    }

    // Gera a expressão e devolve onde o resultado ficou. O Local é um temporário novo, de quem chamou: nunca é o de uma
    // variável, o que preserva a ordem de avaliação em a + (a = 5)
    private Local avaliar(Expressao expressao) {
        if (expressao.getTipo() == TipoVariavel.VAZIO) {
            throw new BugCompilador("Expressão vazia não produz um valor: " + expressao.codigoOriginal());
        }

        return switch (expressao) {
            case ExpressaoInteiroLiteral inteiro ->
                imediato(Banco.INTEIROS, inteiro.getConteudo(), "inteiro literal " + inteiro.getConteudo());
            case ExpressaoCaractereLiteral caractere ->
                imediato(Banco.INTEIROS, caractere.getConteudo(), "caractere literal " + caractere.getConteudo());
            case ExpressaoFlutuanteLiteral flutuante -> imediato(
                    Banco.FLUTUANTES, Float.floatToRawIntBits(flutuante.getConteudo()),
                    "flutuante literal " + flutuante.getConteudo()
            );
            case ExpressaoStringLiteral string -> produzir(Banco.INTEIROS, destino ->
                    gerador.instrucao("la", destino, labelString(string)).comentario("endereço de string literal")
            );
            case ExpressaoIdentificador identificador -> avaliarIdentificador(identificador);
            case ExpressaoAtribuicao atribuicao ->
                atribuir(atribuicao.getIdentificador(), avaliar(atribuicao.getExpressaoLadoDireito()));
            case ExpressaoBinaria binaria -> avaliarBinaria(binaria);
            case ExpressaoUnaria unaria -> avaliarUnaria(unaria);
            case ExpressaoTernaria ternaria -> avaliarTernaria(ternaria);
            case ExpressaoChamadaFuncao chamada -> gerarChamada(chamada);
            case ExpressaoConversao conversao -> avaliarConversao(conversao);
            case ExpressaoVazio ignored -> throw new BugCompilador("Expressão vazia não produz um valor");
        };
    }

    private static Banco banco(Expressao expressao) {
        return Banco.de(expressao.getTipo());
    }

    // Aloca o temporário do resultado e gera o código que o calcula. O código recebe o registrador onde escrever: o do
    // próprio temporário ou, se ele mora num slot de spill, o de recarga 0, que é então guardado no slot
    private Local produzir(Banco banco, Consumer<RegMIPS32> gerarCalculo) {
        Local destino = alocador.alocar(banco, Preferencia.CURTA);
        RegMIPS32 registrador = switch (destino) {
            case Local.Registrador(RegMIPS32 r) -> r;
            case Local.Memoria ignored -> AlocadorRegistradores.recarga(banco, 0);
            case Local.Global global -> throw new BugCompilador("Temporário alocado numa global: " + global);
        };
        gerarCalculo.accept(registrador);
        escrever(registrador, destino);
        return destino;
    }

    private Local imediato(Banco banco, int valor, String descricao) {
        return produzir(banco, destino -> carregarImediato(valor, destino, descricao));
    }

    // O li só escreve em registradores inteiros. Para um destino flutuante, os bits passam pela recarga inteira
    private void carregarImediato(int imediato, RegMIPS32 destino, String descricao) {
        if (destino.banco() == Banco.INTEIROS) {
            gerador.instrucao("li", destino, imediato).comentario(descricao);
            return;
        }

        RegMIPS32 bits = AlocadorRegistradores.recarga(Banco.INTEIROS, 0);
        gerador.instrucao("li", bits, imediato).comentario("bits do " + descricao);
        copiarPalavra(bits, destino);
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

    // Avalia o índice e devolve um temporário com o endereço do elemento: base + índice * tamanho do elemento.
    // Toda leitura e escrita de elemento de vetor passa por aqui, então é aqui que a checagem de limites da #20 entra
    private Local enderecoElemento(ExpressaoIdentificador vetor) {
        SimboloVariavel simbolo = vetor.getSimbolo();
        Local indice = avaliar(vetor.getIndex());
        // O destino é alocado com o índice ainda vivo, para não ser o mesmo registrador: a base é escrita antes de o
        // índice ser somado
        Local endereco = produzir(Banco.INTEIROS, destino -> {
            RegMIPS32 registradorIndice = ler(indice, Banco.INTEIROS, 1);
            // Elementos de 1 byte (car) não precisam de multiplicação
            if (FrameMIPS32.getEspacoMemoria(simbolo.getTipo()) == 4) {
                gerador.instrucao("sll", registradorIndice, registradorIndice, 2).comentario("índice * 4 bytes");
            }
            carregaEnderecoVetor(simbolo, destino);
            gerador.instrucao("addu", destino, destino, registradorIndice)
                    .comentario("endereço de " + simbolo.getNome() + "[índice]");
        });
        alocador.liberar(indice);
        return endereco;
    }

    // O valor de uma variável ou de um elemento de vetor, copiado para um temporário
    private Local avaliarIdentificador(ExpressaoIdentificador identificador) {
        SimboloVariavel simbolo = identificador.getSimbolo();
        Banco banco = Banco.de(simbolo.getTipo());

        if (!simbolo.isVetor()) {
            return produzir(banco, destino -> lerVariavel(destino, simbolo, enderecoEscalar(simbolo)));
        }

        Local endereco = enderecoElemento(identificador);
        RegMIPS32 registradorEndereco = ler(endereco, Banco.INTEIROS, 0);
        // Liberado antes de alocar o destino: o resultado pode reaproveitar o registrador do endereço
        alocador.liberar(endereco);
        return produzir(
                banco, destino -> lerVariavel(destino, simbolo, new Deslocamento(registradorEndereco, 0))
        );
    }

    // Na memória um car ocupa 1 byte, e os demais tipos uma palavra. O car é lido com lbu, sem estender o sinal, para
    // valer o mesmo em qualquer lugar (ver #22)
    private void lerVariavel(RegMIPS32 destino, SimboloVariavel simbolo, Object endereco) {
        String instrucao = switch (simbolo.getTipo()) {
            case INTEIRO, FLUTUANTE -> carga(destino);
            case CARACTERE -> "lbu";
            case STRING, VAZIO -> throw new BugCompilador("Leitura não suportada para " + simbolo.getTipo());
        };
        gerador.instrucao(instrucao, destino, endereco).comentario("lendo " + simbolo.getNome());
    }

    // Par de lerVariavel: grava 1 byte para car e uma palavra para os demais tipos
    private void escreverVariavel(RegMIPS32 valor, SimboloVariavel simbolo, Object endereco) {
        String instrucao = switch (simbolo.getTipo()) {
            case INTEIRO, FLUTUANTE, STRING -> armazenamento(valor);
            case CARACTERE -> "sb";
            case VAZIO -> throw new BugCompilador("Escrita não suportada para " + simbolo.getTipo());
        };
        gerador.instrucao(instrucao, valor, endereco).comentario("armazena " + simbolo.getNome());
    }

    private String labelString(ExpressaoStringLiteral string) {
        String valor = string.getConteudo();
        String label = stringsParaLabels.get(valor);

        if (label == null) {
            label = novoLabel("string");
            gerador.gerarVarGlobal(label + ": .asciiz \"" + valor + "\"");
            stringsParaLabels.put(valor, label);
        }
        return label;
    }

    private void gerarNovalinha() {
        gerador.instrucao("li", RegMIPS32.V0, 11).comentario("novaLinha");
        gerador.instrucao("li", RegMIPS32.A0, 10);
        gerador.instrucao("syscall").comentario("fim novaLinha");
    }

    private Local avaliarBinaria(ExpressaoBinaria expressao) {
        OperadorBinario operador = expressao.getOperador();

        if (operador.getCategoria() == OperadorBinario.Categoria.LOGICA) {
            return avaliarLogica(expressao, operador == OperadorBinario.E ? "beqz" : "bnez");
        }

        String nomeOperacao = operador.getDescricao();
        gerador.comentario(nomeOperacao);

        Expressao esquerda = expressao.getEsquerda();
        Expressao direita = expressao.getDireita();
        Local valorEsquerdo = avaliar(esquerda);
        Local valorDireito = avaliar(direita);

        // Os dois operandos são lidos antes de o resultado ser escrito, então o resultado pode reaproveitar o
        // registrador de qualquer um deles
        RegMIPS32 registradorEsquerdo = ler(valorEsquerdo, banco(esquerda), 0);
        RegMIPS32 registradorDireito = ler(valorDireito, banco(direita), 1);
        alocador.liberar(valorEsquerdo);
        alocador.liberar(valorDireito);

        // Entre int e flut, o int é convertido para flut, na recarga de ponto flutuante do mesmo lado
        if (operador.getCategoria() == OperadorBinario.Categoria.ARITMETICA && expressao.getTipo().isFlutuante()) {
            registradorEsquerdo = paraFlutuante(registradorEsquerdo, 0, "lado esquerdo");
            registradorDireito = paraFlutuante(registradorDireito, 1, "lado direito");
        }

        RegMIPS32 esquerdo = registradorEsquerdo;
        RegMIPS32 direito = registradorDireito;
        Local resultado = produzir(banco(expressao), destino -> {
            switch (operador.getCategoria()) {
                case ARITMETICA -> gerarOperacaoAritmetica(expressao, destino, esquerdo, direito);
                case INTEIRA -> gerarOperacaoInteira(expressao, destino, esquerdo, direito);
                case COMPARACAO -> gerarComparacao(operador, esquerda.getTipo(), destino, esquerdo, direito);
                case LOGICA -> throw new BugCompilador("Operador lógico já tratado");
            }
        });

        gerador.comentario("fim " + nomeOperacao);
        return resultado;
    }

    // Converte o int no registrador para flut, na recarga de ponto flutuante i. Um flut fica como está
    private RegMIPS32 paraFlutuante(RegMIPS32 registrador, int recarga, String descricao) {
        if (registrador.banco() == Banco.FLUTUANTES) {
            return registrador;
        }
        RegMIPS32 convertido = AlocadorRegistradores.recarga(Banco.FLUTUANTES, recarga);
        gerador.instrucao("mtc1", registrador, convertido).comentario("convertendo " + descricao + " para flut");
        gerador.instrucao("cvt.s.w", convertido, convertido);
        return convertido;
    }

    private void gerarOperacaoAritmetica(
            ExpressaoBinaria expressao, RegMIPS32 destino, RegMIPS32 esquerdo, RegMIPS32 direito
    ) {
        OperadorBinario operador = expressao.getOperador();
        boolean flutuante = destino.banco() == Banco.FLUTUANTES;

        switch (operador) {
            case SOMA -> gerador.instrucao(flutuante ? "add.s" : "add", destino, esquerdo, direito)
                    .comentario("soma");
            case SUBTRACAO -> gerador.instrucao(flutuante ? "sub.s" : "sub", destino, esquerdo, direito)
                    .comentario("subtração");
            case MULTIPLICACAO -> gerador.instrucao(flutuante ? "mul.s" : "mul", destino, esquerdo, direito)
                    .comentario("multiplicação");
            case DIVISAO -> {
                if (flutuante) {
                    // Divisão de flutuantes por zero segue o IEEE 754 e resulta em Infinity ou NaN, sem erro
                    gerador.instrucao("div.s", destino, esquerdo, direito).comentario("divisão");
                } else {
                    gerarChecagemDivisaoPorZero(expressao, direito);
                    gerador.instrucao("div", esquerdo, direito).comentario("divisão");
                    gerador.instrucao("mflo", destino).comentario("quociente");
                }
            }
            default -> throw new BugCompilador("Operador '%s' não é aritmético".formatted(operador));
        }
    }

    private void gerarOperacaoInteira(
            ExpressaoBinaria expressao, RegMIPS32 destino, RegMIPS32 esquerdo, RegMIPS32 direito
    ) {
        if (expressao.getOperador() != OperadorBinario.RESTO) {
            throw new BugCompilador("Operador '%s' não é uma operação inteira".formatted(expressao.getOperador()));
        }

        gerarChecagemDivisaoPorZero(expressao, direito);
        gerador.instrucao("div", esquerdo, direito).comentario("divisão");
        gerador.instrucao("mfhi", destino).comentario("resto da divisão");
    }

    // A instrução div do MIPS não gera exceção com divisor zero, o resultado é só indefinido.
    // Por isso checamos o divisor antes e, se for zero, encerramos o programa com erro
    private void gerarChecagemDivisaoPorZero(ExpressaoBinaria expressao, RegMIPS32 divisor) {
        String labelOk = novoLabel("divisaoOk");

        gerador.instrucao("bnez", divisor, labelOk).comentario("checa divisão por zero");
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

    // Compara os dois operandos e coloca 1 ou 0 no destino
    private void gerarComparacao(
            OperadorBinario operador, TipoVariavel tipo, RegMIPS32 destino, RegMIPS32 esquerdo, RegMIPS32 direito
    ) {
        if (tipo.isFlutuante()) {
            // O coprocessador só compara com ==, < e <=. Os demais trocam a ordem dos operandos ou invertem o resultado
            switch (operador) {
                case IGUAL, DIFERENTE -> gerador.instrucao("c.eq.s", esquerdo, direito).comentario("igual");
                case MENOR -> gerador.instrucao("c.lt.s", esquerdo, direito).comentario("menor");
                case MAIOR -> gerador.instrucao("c.lt.s", direito, esquerdo).comentario("maior");
                case MENOR_IGUAL -> gerador.instrucao("c.le.s", esquerdo, direito).comentario("menor ou igual");
                case MAIOR_IGUAL -> gerador.instrucao("c.le.s", direito, esquerdo).comentario("maior ou igual");
                default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
            }
            guardaComparacaoFloat(destino, operador == OperadorBinario.DIFERENTE);
            return;
        }

        switch (operador) {
            case IGUAL -> {
                gerador.instrucao("sub", destino, esquerdo, direito).comentario("diferença");
                gerador.instrucao("sltiu", destino, destino, 1).comentario("1 se a diferença é 0");
            }
            case DIFERENTE -> {
                gerador.instrucao("sub", destino, esquerdo, direito).comentario("diferença");
                gerador.instrucao("sltu", destino, RegMIPS32.ZERO, destino).comentario("1 se a diferença não é 0");
            }
            case MENOR -> gerador.instrucao("slt", destino, esquerdo, direito).comentario("menor");
            case MAIOR -> gerador.instrucao("slt", destino, direito, esquerdo).comentario("maior");
            case MENOR_IGUAL -> {
                gerador.instrucao("slt", destino, direito, esquerdo).comentario("maior");
                gerador.instrucao("xori", destino, destino, 1).comentario("inverte para menor ou igual");
            }
            case MAIOR_IGUAL -> {
                gerador.instrucao("slt", destino, esquerdo, direito).comentario("menor");
                gerador.instrucao("xori", destino, destino, 1).comentario("inverte para maior ou igual");
            }
            default -> throw new BugCompilador("Operador '%s' não é de comparação".formatted(operador));
        }
    }

    private void guardaComparacaoFloat(RegMIPS32 destino, boolean inverter) {
        String labelFalso = novoLabel("comparacaoFalso");
        String labelFim = novoLabel("comparacaoFim");

        gerador.instrucao("bc1f", labelFalso);
        gerador.instrucao("li", destino, inverter ? 0 : 1);
        gerador.instrucao("j", labelFim);
        gerador.label(labelFalso);
        gerador.instrucao("li", destino, inverter ? 1 : 0);
        gerador.label(labelFim);
    }

    // Avalia com curto-circuito: se o lado esquerdo já decide o resultado (falso para 'e', verdadeiro para 'ou'),
    // o lado direito não é avaliado. O resultado fica no temporário do lado esquerdo nos dois caminhos (o direito é
    // copiado para lá) e é normalizado para 0 ou 1 depois da junção
    private Local avaliarLogica(ExpressaoBinaria expressao, String instrucaoCurtoCircuito) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        String labelCurtoCircuito = novoLabel("logicoCurto");

        gerador.comentario(nomeOperacao);

        Local resultado = avaliar(expressao.getEsquerda());
        gerador.instrucao(instrucaoCurtoCircuito, ler(resultado, Banco.INTEIROS, 0), labelCurtoCircuito)
                .comentario("curto-circuito");

        Local direito = avaliar(expressao.getDireita());
        mover(direito, resultado, Banco.INTEIROS);
        alocador.liberar(direito);

        gerador.label(labelCurtoCircuito);
        RegMIPS32 registrador = ler(resultado, Banco.INTEIROS, 0);
        gerador.instrucao("sltu", registrador, RegMIPS32.ZERO, registrador)
                .comentario("normaliza o resultado para 0 ou 1");
        escrever(registrador, resultado);

        gerador.comentario("fim " + nomeOperacao);
        return resultado;
    }

    // A análise semântica só deixa passar conversões para o mesmo tipo, que não geram código, e entre int e flut
    private Local avaliarConversao(ExpressaoConversao conversao) {
        Local valor = avaliar(conversao.getExpressao());
        TipoVariavel origem = conversao.getExpressao().getTipo();
        TipoVariavel destino = conversao.getDestino();

        if (origem == destino) {
            return valor;
        }

        RegMIPS32 registrador = ler(valor, Banco.de(origem), 0);
        alocador.liberar(valor);

        if (origem == TipoVariavel.INTEIRO && destino == TipoVariavel.FLUTUANTE) {
            return produzir(Banco.FLUTUANTES, convertido -> {
                gerador.instrucao("mtc1", registrador, convertido);
                gerador.instrucao("cvt.s.w", convertido, convertido).comentario("converte int para flut");
            });
        }
        if (origem == TipoVariavel.FLUTUANTE && destino == TipoVariavel.INTEIRO) {
            // O resultado do trunc é um int, mas ainda num registrador de ponto flutuante: passa pela recarga 1, que
            // não é a do valor lido
            RegMIPS32 truncado = AlocadorRegistradores.recarga(Banco.FLUTUANTES, 1);
            return produzir(Banco.INTEIROS, convertido -> {
                gerador.instrucao("trunc.w.s", truncado, registrador)
                        .comentario("converte flut para int, truncando em direção a zero");
                gerador.instrucao("mfc1", convertido, truncado);
            });
        }
        throw new BugCompilador("Conversão de " + origem + " para " + destino + " passou pela análise semântica");
    }

    private Local avaliarUnaria(ExpressaoUnaria expressao) {
        String nomeOperacao = expressao.getOperador().getDescricao();
        Banco banco = banco(expressao.getExpressao());

        gerador.comentario("inicio " + nomeOperacao);

        Local valor = avaliar(expressao.getExpressao());
        RegMIPS32 registrador = ler(valor, banco, 0);
        alocador.liberar(valor);

        Local resultado = produzir(banco, destino -> {
            switch (expressao.getOperador()) {
                case NEGACAO -> gerador.instrucao("seq", destino, registrador, RegMIPS32.ZERO)
                        .comentario("inverte o valor booleano");
                case NEGATIVO -> {
                    if (banco == Banco.FLUTUANTES) {
                        gerador.instrucao("neg.s", destino, registrador).comentario("inverte o sinal do número");
                    } else {
                        gerador.instrucao("sub", destino, RegMIPS32.ZERO, registrador)
                                .comentario("inverte o sinal do número");
                    }
                }
            }
        });

        gerador.comentario("fim " + nomeOperacao);
        return resultado;
    }

    // Os dois ramos deixam o resultado no mesmo Local: o do ramo "se", que continua ocupado enquanto o ramo "senão" é
    // avaliado, e recebe uma cópia dele
    private Local avaliarTernaria(ExpressaoTernaria expressao) {
        gerador.comentario("inicio expressão ternária");

        String labelFalso = novoLabel("ternarioFalso");
        String labelFim = novoLabel("ternarioFim");

        Local condicao = avaliar(expressao.getCondicao());
        gerador.instrucao("beqz", ler(condicao, Banco.INTEIROS, 0), labelFalso);
        alocador.liberar(condicao);

        Local resultado = avaliar(expressao.getSe());
        gerador.instrucao("j", labelFim);

        gerador.label(labelFalso);
        Local senao = avaliar(expressao.getSenao());
        mover(senao, resultado, banco(expressao));
        alocador.liberar(senao);

        gerador.label(labelFim);
        gerador.comentario("fim expressão ternária");
        return resultado;
    }

    // Atribui o valor à variável alvo e o devolve, pois a atribuição também é uma expressão: quem a usa (outra
    // expressão, ou o comando que a contém) é que o libera. O valor é avaliado antes do índice
    private Local atribuir(ExpressaoIdentificador alvo, Local valor) {
        SimboloVariavel simbolo = alvo.getSimbolo();
        Banco banco = Banco.de(simbolo.getTipo());

        if (simbolo.isVetor()) {
            Local endereco = enderecoElemento(alvo);
            RegMIPS32 registradorEndereco = ler(endereco, Banco.INTEIROS, 0);
            escreverVariavel(ler(valor, banco, 1), simbolo, new Deslocamento(registradorEndereco, 0));
            alocador.liberar(endereco);
        } else {
            escreverVariavel(ler(valor, banco, 0), simbolo, enderecoEscalar(simbolo));
        }
        return valor;
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
        Local valor = avaliar(comandoEscreva.getExpressao());

        gerador.comentario("inicio escreva");

        switch (tipo) {
            case INTEIRO -> gerador.instrucao("li", RegMIPS32.V0, 1);
            case CARACTERE -> gerador.instrucao("li", RegMIPS32.V0, 11);
            case STRING -> gerador.instrucao("li", RegMIPS32.V0, 4);
            case FLUTUANTE -> gerador.instrucao("li", RegMIPS32.V0, 2);
            case VAZIO -> throw new BugCompilador("Escrita de valor vazio passou pela análise semântica");
        }

        copiarPara(valor, tipo.isFlutuante() ? RegMIPS32.F12 : RegMIPS32.A0);
        gerador.instrucao("syscall").comentario("fim escreva");

        alocador.liberar(valor);
    }

    // Ler um valor é atribuí-lo à variável. Ele sai do $v0/$f0 para um temporário antes do índice ser avaliado, porque
    // o índice pode ter uma chamada ou uma syscall
    private void gerarLeia(ComandoLeia comandoLeia) {
        gerador.comentario("inicio comando leia");

        ExpressaoIdentificador alvo = comandoLeia.getExpressaoIdentificador();
        TipoVariavel tipo = alvo.getSimbolo().getTipo();
        int syscall = switch (tipo) {
            case INTEIRO -> 5;
            case CARACTERE -> 12;
            case FLUTUANTE -> 6;
            default -> throw new BugCompilador("Leitura de " + tipo + " passou pela análise semântica");
        };
        gerador.instrucao("li", RegMIPS32.V0, syscall).comentario("syscall para ler " + tipo);
        gerador.instrucao("syscall");

        Local lido = resultadoEm(tipo.isFlutuante() ? RegMIPS32.F0 : RegMIPS32.V0);
        alocador.liberar(atribuir(alvo, lido));

        gerador.comentario("fim comando leia");
    }

    // Um temporário novo com o valor do registrador fixo (ex: $v0 depois de uma chamada ou syscall)
    private Local resultadoEm(RegMIPS32 registrador) {
        Local destino = alocador.alocar(registrador.banco(), Preferencia.CURTA);
        escrever(registrador, destino);
        return destino;
    }

    // O resultado da chamada, ou null se a função é vazio
    private Local gerarChamada(ExpressaoChamadaFuncao chamada) {
        return switch (chamada.getFuncao()) {
            case SimboloFuncaoNativa nativa -> gerarChamadaNativa(chamada, nativa.funcao());
            case SimboloFuncao funcao -> gerarChamadaFuncao(chamada, funcao.declaracao());
        };
    }

    // Cada função nativa é implementada do jeito que for mais simples para o MIPS:
    // - intrínseca: o código é gerado no lugar da chamada, sem jal
    // - rotina de biblioteca: o argumento vai em $a0 e o resultado volta em $v0
    private Local gerarChamadaNativa(ExpressaoChamadaFuncao chamada, FuncaoNativa funcao) {
        Expressao argumento = chamada.getArgumentos().getFirst();
        Local valor = avaliar(argumento);

        return switch (funcao) {
            case PISO -> {
                // Uma instrução de FPU resolve: não vale o custo de uma chamada
                gerador.comentario("piso (intrínseca)");
                RegMIPS32 registrador = ler(valor, Banco.FLUTUANTES, 0);
                alocador.liberar(valor);
                RegMIPS32 arredondado = AlocadorRegistradores.recarga(Banco.FLUTUANTES, 1);
                yield produzir(Banco.INTEIROS, destino -> {
                    gerador.instrucao("floor.w.s", arredondado, registrador)
                            .comentario("arredonda para baixo, resultado inteiro num registrador de flut");
                    gerador.instrucao("mfc1", destino, arredondado);
                });
            }
            case RAND -> {
                // A rotina só usa $a0, $a1 e $v0, então os temporários vivos não precisam ser salvos
                gerador.comentario("rand (rotina de biblioteca)");
                gerador.incluirRotina(RotinaMIPS32.RAND);
                copiarPara(valor, RegMIPS32.A0);
                alocador.liberar(valor);
                gerador.instrucao("jal", RotinaMIPS32.RAND.label);
                yield resultadoEm(RegMIPS32.V0);
            }
            case RAIZ -> {
                gerador.comentario("raiz (intrínseca)");
                RegMIPS32 registrador = ler(valor, Banco.FLUTUANTES, 0);
                alocador.liberar(valor);
                yield produzir(Banco.FLUTUANTES, destino -> gerador.instrucao("sqrt.s", destino, registrador));
            }
            // Obriga o compilador a exigir um case para cada FuncaoNativa
            case null -> throw new BugCompilador("Chamada de função nativa sem a função");
        };
    }

    // Os argumentos são avaliados em temporários, que ficam vivos até o jal: um argumento seguinte pode chamar outra
    // função, e aí os anteriores são salvos como qualquer temporário vivo. Só logo antes do jal eles vão para os $a e
    // para os slots da área de argumentos.
    // Os temporários que continuam vivos depois da chamada (ex: o "a" em a + f(b)) são caller-saved: ficam na pilha,
    // acima da área de argumentos, durante a chamada
    private Local gerarChamadaFuncao(ExpressaoChamadaFuncao chamada, DeclaracaoFuncao funcao) {
        String nomeFuncao = funcao.getNome();
        List<Expressao> argumentos = chamada.getArgumentos();
        AreaArgumentos area = areaArgumentos(funcao);

        gerador.comentario("inicio chamada da função " + nomeFuncao);

        List<Local> valores = new ArrayList<>(argumentos.size());
        for (int i = 0; i < argumentos.size(); i++) {
            valores.add(
                    funcao.getParametros().get(i).isVetor()
                            ? enderecoVetor(argumentos.get(i))
                            : avaliar(argumentos.get(i))
            );
        }

        // Os argumentos são consumidos pela chamada, então não precisam ser salvos
        List<RegMIPS32> vivos = new ArrayList<>(alocador.temporariosOcupados());
        for (Local valor : valores) {
            if (valor instanceof Local.Registrador(RegMIPS32 registrador)) {
                vivos.remove(registrador);
            }
        }
        empilharTodos(vivos);

        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -area.tamanho())
                .comentario("reserva a área de argumentos");
        for (int i = 0; i < valores.size(); i++) {
            Local valor = valores.get(i);
            switch (area.passagens().get(i)) {
                case PassagemArgumentos.Registrador(RegMIPS32 registrador, int ignored) ->
                    copiarPara(valor, registrador);
                case PassagemArgumentos.Pilha(int offset) -> {
                    RegMIPS32 registrador = ler(valor, bancoDoLocal(valor, funcao.getParametros().get(i)), 0);
                    gerador.instrucao(armazenamento(registrador), registrador, new Deslocamento(RegMIPS32.SP, offset))
                            .comentario("argumento " + (i + 1) + " no slot dele");
                }
            }
            alocador.liberar(valor);
        }

        gerador.instrucao("jal", labelFuncao(nomeFuncao)).comentario("chama a função " + nomeFuncao);
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, area.tamanho())
                .comentario("libera a área de argumentos");

        // Os temporários restaurados nunca são o do resultado: continuaram ocupados durante a chamada
        Local resultado = funcao.getTipoRetorno().isTipoVazio() ? null : resultadoEm(registradorRetorno(funcao));
        desempilharTodos(vivos);

        gerador.comentario("fim chamada da função " + nomeFuncao);
        return resultado;
    }

    private static Banco bancoDoLocal(Local valor, ParametroFuncao parametro) {
        if (valor instanceof Local.Registrador(RegMIPS32 registrador)) {
            return registrador.banco();
        }
        return Banco.de(parametro.getSimbolo().getTipo(), parametro.isVetor());
    }

    private void empilharTodos(List<RegMIPS32> registradores) {
        if (registradores.isEmpty()) {
            return;
        }
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, -FrameMIPS32.TAMANHO_PALAVRA * registradores.size())
                .comentario("espaço para os temporários vivos durante a chamada");
        for (int i = 0; i < registradores.size(); i++) {
            RegMIPS32 registrador = registradores.get(i);
            gerador.instrucao(
                    armazenamento(registrador), registrador,
                    new Deslocamento(RegMIPS32.SP, FrameMIPS32.TAMANHO_PALAVRA * i)
            ).comentario("salva " + registrador);
        }
    }

    private void desempilharTodos(List<RegMIPS32> registradores) {
        if (registradores.isEmpty()) {
            return;
        }
        for (int i = 0; i < registradores.size(); i++) {
            RegMIPS32 registrador = registradores.get(i);
            gerador.instrucao(
                    carga(registrador), registrador, new Deslocamento(RegMIPS32.SP, FrameMIPS32.TAMANHO_PALAVRA * i)
            ).comentario("restaura " + registrador);
        }
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.SP, FrameMIPS32.TAMANHO_PALAVRA * registradores.size());
    }

    // Prólogo: os argumentos que chegaram em registrador vão para o slot deles na home area. Daí em diante a função lê
    // todos os parâmetros da memória, como as variáveis locais
    private void salvaArgumentosEmRegistrador(DeclaracaoFuncao funcao) {
        List<ParametroFuncao> parametros = funcao.getParametros();
        List<PassagemArgumentos> passagens = areaArgumentos(funcao).passagens();

        for (int i = 0; i < parametros.size(); i++) {
            if (passagens.get(i) instanceof PassagemArgumentos.Registrador(RegMIPS32 registrador, int ignored)) {
                gerador.instrucao(
                        armazenamento(registrador), registrador,
                        new Deslocamento(RegMIPS32.FP, frameAtual.offset(parametros.get(i)))
                ).comentario("salva " + parametros.get(i).getNome() + " na home area");
            }
        }
    }

    // A área só depende dos parâmetros. Fica calculada para cada função, pela chamada ou pelo prólogo, o que vier
    // primeiro: uma função pode ser chamada antes de o código dela ser gerado
    private AreaArgumentos areaArgumentos(DeclaracaoFuncao funcao) {
        return areasArgumentos.computeIfAbsent(funcao, f -> AreaArgumentos.de(f.getParametros()));
    }

    private static RegMIPS32 registradorRetorno(DeclaracaoFuncao funcao) {
        return funcao.getTipoRetorno().getTipo().isFlutuante() ? RegMIPS32.F0 : RegMIPS32.V0;
    }

    // O endereço do primeiro elemento do vetor, que é como vetores são passados para funções.
    // A análise semântica garante que o argumento é o nome de um vetor, possivelmente entre parênteses
    private Local enderecoVetor(Expressao argumento) {
        if (!(argumento instanceof ExpressaoIdentificador identificador)) {
            throw new BugCompilador("Argumento vetor não é um identificador: " + argumento.codigoOriginal());
        }
        return produzir(Banco.INTEIROS, destino -> carregaEnderecoVetor(identificador.getSimbolo(), destino));
    }

    private void gerarRetorno(ComandoRetorno comandoRetorno) {
        gerador.comentario("inicio comando retorno");

        Expressao expressao = comandoRetorno.getExpressao();
        Local valor = expressao.getTipo() == TipoVariavel.VAZIO ? null : avaliar(expressao);

        if (funcaoAtual == null) {
            // No bloco programa, o valor retornado é o código de saída
            if (valor != null) {
                copiarPara(valor, RegMIPS32.A0);
            } else {
                gerador.instrucao("li", RegMIPS32.A0, 0).comentario("retorne sem valor sai com código 0");
            }

            gerador.instrucao("li", RegMIPS32.V0, 17)
                    .comentario("syscall para finalizar o programa com código de retorno");
            gerador.instrucao("syscall").comentario("fim main");
        } else {
            if (valor != null) {
                copiarPara(valor, registradorRetorno(funcaoAtual));
            }
            gerador.instrucao("j", labelEpilogo(funcaoAtual.getNome()));
        }

        if (valor != null) {
            alocador.liberar(valor);
        }
        gerador.comentario("fim comando retorno");
    }

    // Epílogo da função: restaura os registradores salvos e o $ra, descarta o frame, o $ra e o $fp salvos, restaura o
    // $fp e retorna. A área de argumentos é de quem chamou, que a libera depois do jal. Como o $fp não muda durante a
    // função, todo retorne salta para cá, em qualquer profundidade de blocos.
    // O $fp antigo é lido depois de o $sp subir: liberar não apaga a memória, e no MARS nada escreve abaixo do $sp
    // entre as duas instruções. Num sistema com signal handlers isso não vale, e as leituras teriam que vir antes
    // do addiu
    private void finalizarFuncao(String nomeFuncao, List<RegMIPS32> salvos) {
        if (funcaoAtual == null) {
            throw new BugCompilador("Deveríamos estar dentro da função " + nomeFuncao);
        }
        gerador.label(labelEpilogo(nomeFuncao));
        for (int i = 0; i < salvos.size(); i++) {
            RegMIPS32 salvo = salvos.get(i);
            gerador.instrucao(carga(salvo), salvo, enderecoSalvo(i)).comentario("restaura " + salvo);
        }
        gerador.instrucao("lw", RegMIPS32.RA, new Deslocamento(RegMIPS32.FP, 4))
                .comentario("restaura o endereço de retorno");
        gerador.instrucao("addiu", RegMIPS32.SP, RegMIPS32.FP, 8)
                .comentario("descarta frame, $ra e $fp salvos");
        gerador.instrucao("lw", RegMIPS32.FP, new Deslocamento(RegMIPS32.FP, 0))
                .comentario("restaura o $fp");
        gerador.instrucao("jr", RegMIPS32.RA).comentario("retornando da função " + nomeFuncao);
    }

    private void gerarSe(ComandoSe comandoSe) {
        String labelFalso = novoLabel("seFalso");
        String labelFim = novoLabel("seFim");

        gerador.comentario("inicio comando se");

        Local condicao = avaliar(comandoSe.getSe());
        gerador.instrucao("beqz", ler(condicao, Banco.INTEIROS, 0), labelFalso);
        alocador.liberar(condicao);

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

        Local condicao = avaliar(comandoEnquanto.getCondicional());
        gerador.instrucao("beqz", ler(condicao, Banco.INTEIROS, 0), labelFim);
        alocador.liberar(condicao);

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

    // O registrador onde o valor está para ser lido por uma instrução; se ele está na memória, é carregado no
    // registrador de recarga i do banco
    private RegMIPS32 ler(Local local, Banco banco, int recarga) {
        return switch (local) {
            case Local.Registrador(RegMIPS32 registrador) -> registrador;
            case Local.Memoria memoria -> carregar(memoria.deslocamento(), AlocadorRegistradores.recarga(banco, recarga));
            case Local.Global global -> carregar(global.label(), AlocadorRegistradores.recarga(banco, recarga));
        };
    }

    private RegMIPS32 carregar(Object endereco, RegMIPS32 registrador) {
        gerador.instrucao(carga(registrador), registrador, endereco);
        return registrador;
    }

    // Guarda o valor do registrador no Local: uma cópia entre registradores ou uma escrita na memória
    private void escrever(RegMIPS32 origem, Local destino) {
        switch (destino) {
            case Local.Registrador(RegMIPS32 registrador) -> {
                if (registrador != origem) {
                    copiarPalavra(origem, registrador);
                }
            }
            case Local.Memoria memoria ->
                gerador.instrucao(armazenamento(origem), origem, memoria.deslocamento());
            case Local.Global global -> gerador.instrucao(armazenamento(origem), origem, global.label());
        }
    }

    // Copia o valor de um Local para outro (ex: o resultado de um ramo para o Local da junção)
    private void mover(Local origem, Local destino, Banco banco) {
        if (!origem.equals(destino)) {
            escrever(ler(origem, banco, 1), destino);
        }
    }

    // Copia o valor para um registrador específico (ex: $a0 do syscall, $v0 do retorno). Se ele está na memória,
    // carrega direto no destino, sem passar pela recarga
    private void copiarPara(Local local, RegMIPS32 destino) {
        switch (local) {
            case Local.Registrador(RegMIPS32 origem) -> copiarPalavra(origem, destino);
            case Local.Memoria memoria -> carregar(memoria.deslocamento(), destino);
            case Local.Global global -> carregar(global.label(), destino);
        }
    }

    // Uma palavra entre registradores, inclusive entre bancos (os bits de um flut num $a, por exemplo)
    private void copiarPalavra(RegMIPS32 origem, RegMIPS32 destino) {
        if (origem.banco() == destino.banco()) {
            gerador.instrucao(origem.banco() == Banco.INTEIROS ? "move" : "mov.s", destino, origem);
        } else if (origem.banco() == Banco.INTEIROS) {
            gerador.instrucao("mtc1", origem, destino);
        } else {
            gerador.instrucao("mfc1", destino, origem);
        }
    }

    // Uma palavra inteira entre a memória e o registrador, que pode ser do coprocessador de ponto flutuante
    private static String carga(RegMIPS32 registrador) {
        return registrador.banco() == Banco.FLUTUANTES ? "lwc1" : "lw";
    }

    private static String armazenamento(RegMIPS32 registrador) {
        return registrador.banco() == Banco.FLUTUANTES ? "swc1" : "sw";
    }

    // Labels internas nunca começam com "f_" ou "g_", que são reservados para funções e variáveis globais
    private String novoLabel(String prefixo) {
        return prefixo + "_" + (++contadorLabels);
    }

    // Prefixo evita conflito com labels internas, com "main" e com nomes reservados do montador
    private String labelFuncao(String nomeFuncao) {
        return "f_" + nomeFuncao;
    }

    // Não começa com "f_": não colide com uma função chamada "epilogo_..."
    private String labelEpilogo(String nomeFuncao) {
        return "epilogo_" + labelFuncao(nomeFuncao);
    }

    private String labelVariavelGlobal(String nomeVariavel) {
        return "g_" + nomeVariavel;
    }
}
