package src.raiz.compilador.mips32;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import src.raiz.ast.NoSintatico;
import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.TipoVariavel;
import src.raiz.ast.Variavel;
import src.raiz.ast.comandos.Comando;
import src.raiz.ast.comandos.ComandoBloco;
import src.raiz.ast.comandos.ComandoComExpressao;
import src.raiz.ast.comandos.ComandoContinue;
import src.raiz.ast.comandos.ComandoEnquanto;
import src.raiz.ast.comandos.ComandoEscreva;
import src.raiz.ast.comandos.ComandoLeia;
import src.raiz.ast.comandos.ComandoNovalinha;
import src.raiz.ast.comandos.ComandoPare;
import src.raiz.ast.comandos.ComandoRetorno;
import src.raiz.ast.comandos.ComandoSe;
import src.raiz.ast.comandos.ComandoVazio;
import src.raiz.ast.declaracoes.BlocoDeclaracoes;
import src.raiz.ast.declaracoes.BlocoPrograma;
import src.raiz.ast.declaracoes.DeclaracaoDeVariavel;
import src.raiz.ast.declaracoes.DeclaracaoFuncao;
import src.raiz.erros.BugCompilador;
import src.raiz.semantica.SimboloParametro;
import src.raiz.semantica.SimboloVariavel;
import src.raiz.semantica.SimboloVariavelGlobal;
import src.raiz.semantica.SimboloVariavelLocal;

// Layout do frame de uma função ou do bloco programa: onde cada parâmetro e variável local fica em relação ao $fp,
// e quanto espaço o prólogo precisa reservar. Blocos irmãos nunca existem ao mesmo tempo, então reaproveitam o
// mesmo espaço: o tamanho do frame é o do caminho mais fundo de blocos aninhados, não a soma de todos
final class FrameMIPS32 {

    static final int TAMANHO_PALAVRA = 4;
    // O $ra e o $fp salvos ficam entre o $fp e a área de argumentos
    private static final int OFFSET_INICIO_ARGUMENTOS = 2 * TAMANHO_PALAVRA;

    private final int tamanho;

    // Offset de cada variável local e parâmetro em relação ao $fp, indexado pelo nó da declaração.
    // Parâmetros ficam acima do $fp (offset positivo) e variáveis locais abaixo (negativo)
    private final Map<NoSintatico, Integer> offsetsVariaveis;

    private FrameMIPS32(int tamanho, Map<NoSintatico, Integer> offsetsVariaveis) {
        this.tamanho = tamanho;
        this.offsetsVariaveis = offsetsVariaveis;
    }

    static FrameMIPS32 deFuncao(DeclaracaoFuncao funcao) {
        Map<NoSintatico, Integer> offsetsVariaveis = new IdentityHashMap<>();

        List<ParametroFuncao> parametros = funcao.getParametros();
        // Os parâmetros ficam na área de argumentos de quem chamou, logo acima do $ra e do $fp salvos. Os que chegam
        // em registrador o prólogo salva no slot deles, na home area
        List<LocalArgumento> locais = AreaArgumentos.de(parametros).locais();
        for (int i = 0; i < parametros.size(); i++) {
            offsetsVariaveis.put(parametros.get(i), OFFSET_INICIO_ARGUMENTOS + locais.get(i).offset());
        }

        int tamanho = aloca(funcao.getCorpo(), 0, offsetsVariaveis);

        return new FrameMIPS32(tamanho, offsetsVariaveis);
    }

    static FrameMIPS32 dePrograma(BlocoPrograma programa) {
        Map<NoSintatico, Integer> offsetsVariaveis = new IdentityHashMap<>();
        int tamanho = aloca(programa.getBlocoDeclaracoes(), 0, offsetsVariaveis);

        return new FrameMIPS32(tamanho, offsetsVariaveis);
    }

    // Atribui offsets às variáveis do bloco e dos blocos dentro dele, a partir de offsetBase bytes abaixo do $fp.
    // Retorna até onde o frame desce por causa deste bloco
    private static int aloca(BlocoDeclaracoes bloco, int offsetBase, Map<NoSintatico, Integer> offsetsVariaveis) {
        int offset = offsetBase;

        for (DeclaracaoDeVariavel declaracao : bloco.getVariaveis().getDeclaracoesDeVariaveis()) {
            for (Variavel variavel : declaracao.getVariaveis()) {
                // Soma antes de gravar: a primeira local fica em -4($fp), logo abaixo do $fp antigo em 0($fp)
                offset += tamanhoVariavel(variavel.getSimbolo());
                offsetsVariaveis.put(variavel, -offset);
            }
        }

        int maior = offset;

        for (Comando comando : bloco.getComandos()) {
            maior = Math.max(maior, alocaComando(comando, offset, offsetsVariaveis));
        }

        return maior;
    }

    // Até onde o frame desce por causa dos blocos dentro deste comando
    private static int alocaComando(Comando comando, int base, Map<NoSintatico, Integer> offsets) {
        return switch (comando) {
            case ComandoBloco bloco -> aloca(bloco.getDeclaracoes(), base, offsets);
            case ComandoSe se -> Math.max(
                    alocaComando(se.getConsequencia(), base, offsets),
                    se.getAlternativa() == null ? base : alocaComando(se.getAlternativa(), base, offsets)
            );
            case ComandoEnquanto enquanto -> alocaComando(enquanto.getComando(), base, offsets);
            case ComandoContinue ignored -> base;
            case ComandoPare ignored -> base;
            case ComandoComExpressao ignored -> base;
            case ComandoEscreva ignored -> base;
            case ComandoLeia ignored -> base;
            case ComandoNovalinha ignored -> base;
            case ComandoRetorno ignored -> base;
            case ComandoVazio ignored -> base;
        };
    }

    // Offset de uma variável local ou parâmetro em relação ao $fp
    int offset(SimboloVariavel simbolo) {
        return offset(simbolo.getDeclaracao());
    }

    // Offset em relação ao $fp, a partir do nó da declaração da variável local ou do parâmetro
    int offset(NoSintatico declaracao) {
        Integer offset = offsetsVariaveis.get(declaracao);
        if (offset == null) {
            throw new BugCompilador(
                    "Variável '" + declaracao.getToken().lexema() + "' usada sem espaço reservado no frame"
            );
        }
        return offset;
    }

    int getTamanho() {
        return tamanho;
    }

    // Quantos bytes a variável ocupa: no frame, para locais e parâmetros, ou no segmento de dados, para globais
    static int tamanhoVariavel(SimboloVariavel simbolo) {
        return switch (simbolo) {
            // Um valor, ou o endereço do vetor de quem chamou: os elementos ficam no frame de quem chamou
            case SimboloParametro ignored -> TAMANHO_PALAVRA;
            case SimboloVariavelLocal local -> {
                if (local.isVetor()) {
                    int tamanhoExato = getEspacoMemoria(local.getTipo()) * local.variavel().getTamanhoVetor();
                    // Arredonda para múltiplo de 4, para a variável seguinte continuar alinhada (ex: car c[5] ocupa 8)
                    yield arredondaParaPalavra(tamanhoExato);
                }
                // Escalares ocupam uma palavra, inclusive car: ele é lido e escrito no primeiro byte do espaço
                yield TAMANHO_PALAVRA;
            }
            case SimboloVariavelGlobal global -> {
                int tamanho = getEspacoMemoria(global.getTipo());

                // Sem arredondamento: o .align 2 antes de cada global alinha a seguinte
                if (global.isVetor()) {
                    yield tamanho * global.variavel().getTamanhoVetor();
                } else {
                    yield tamanho;
                }
            }
        };
    }

    // Quantos bytes um valor do tipo ocupa na memória. Na pilha de expressões todo valor ocupa uma palavra
    static int getEspacoMemoria(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, STRING -> 4; // int tem 32 bits; uma string é o endereço dos seus bytes
            case CARACTERE -> 1;
            case FLUTUANTE -> 4;       // precisão simples
            case VAZIO -> throw new BugCompilador("Tipo vazio sendo usado como valor!");
        };
    }

    private static int arredondaParaPalavra(int tamanho) {
        return (tamanho + TAMANHO_PALAVRA - 1) / TAMANHO_PALAVRA * TAMANHO_PALAVRA;
    }
}
