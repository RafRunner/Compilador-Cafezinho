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
import src.raiz.ast.comandos.ComandoEnquanto;
import src.raiz.ast.comandos.ComandoEscreva;
import src.raiz.ast.comandos.ComandoLeia;
import src.raiz.ast.comandos.ComandoNovalinha;
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
        // O último parâmetro é o último empilhado e fica logo acima do $ra salvo; cada anterior fica acima do seguinte
        int offset = 8;
        for (int i = parametros.size() - 1; i >= 0; i--) {
            offsetsVariaveis.put(parametros.get(i), offset);
            offset += tamanhoVariavel(parametros.get(i).getSimbolo());
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
            throw new BugCompilador("Variável '" + declaracao.getToken().lexema() + "' usada sem espaço reservado no frame");
        }
        return offset;
    }

    int getTamanho() {
        return tamanho;
    }

    public static int tamanhoVariavel(SimboloVariavel simbolo) {
        return switch (simbolo) {
            case SimboloParametro ignored -> 4;   // um valor, ou o endereço do vetor de quem chamou
            case SimboloVariavelLocal local -> {
                if (local.isVetor()) {
                    int tamanhoExato = getEspacoMemoria(local.getTipo()) * local.variavel().getTamanhoVetor();
                    // O tamanho no frame precisa ser múltiplo de 4
                    yield (tamanhoExato + 3) / 4 * 4;
                }
                yield 4;
            }
            case SimboloVariavelGlobal global -> {
                int tamanho = getEspacoMemoria(global.getTipo());

                if (global.isVetor()) {
                    // Vetores globais já são alinhados
                    yield tamanho * global.variavel().getTamanhoVetor();
                } else {
                    yield tamanho;
                }
            }
        };
    }

    public static int getEspacoMemoria(TipoVariavel tipo) {
        return switch (tipo) {
            case INTEIRO, STRING -> 4; // int tem 4 bytes -> 32 bits. Uma string é um ponteiro
            case CARACTERE -> 1;       // um caractere é um byte
            case FLUTUANTE -> 4;       // os floats são de precissão única
            case VAZIO -> throw new BugCompilador("Tipo vazio sendo usado como valor!");
        };
    }
}
