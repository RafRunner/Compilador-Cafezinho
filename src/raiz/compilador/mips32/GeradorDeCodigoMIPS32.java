package src.raiz.compilador.mips32;

import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.ModoGerador;
import src.raiz.erros.BugCompilador;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.StringJoiner;

// Gerador de código específico para a arquitetura MIPS 32
class GeradorDeCodigoMIPS32 extends GeradorDeCodigo {

    private final Set<RotinaMIPS32> rotinasIncluidas = EnumSet.noneOf(RotinaMIPS32.class);

    public GeradorDeCodigoMIPS32() {
        // Iniciando seção de variáveis globais
        gerarVarGlobal(".data");

        // Iniciando seção de código
        gerarMain(".text");
    }

    // Permite comentar a instrução que acabou de ser emitida. O comentário vai para o fim da mesma linha do assembly,
    // que é o que o MARS mostra ao executar passo a passo
    public static class AjusteInstrucao {
        private final int index;
        private final List<String> comandos;

        // Criado logo depois de a instrução ser adicionada, então ela é a última linha
        private AjusteInstrucao(List<String> comandos) {
            this.index = comandos.size() - 1;
            this.comandos = comandos;
        }

        public AjusteInstrucao comentario(String comentario) {
            comandos.set(index, comandos.get(index) + " # " + comentario);
            return this;
        }
    }

    public AjusteInstrucao instrucao(String mnemonico, Object... operandos) {
        StringJoiner operandosFormatados = new StringJoiner(", ");
        for (Object operando : operandos) {
            operandosFormatados.add(operando.toString());
        }
        String linha = "%-6s%s".formatted(
                mnemonico,
                operandosFormatados.length() == 0 ? "" : " " + operandosFormatados
        );
        gerar(linha);

        return new AjusteInstrucao(linhasDoModoAtual());
    }

    public void comentario(String comentario) {
        gerar("# " + comentario);
    }

    public void label(String label) {
        gerar(label + ":");
    }

    public void diretiva(String diretiva) {
        gerar(diretiva);
    }

    @Override
    public String geraCodigoObjeto() {
        return Stream.of(instrucoesVariaveisGlobais, instrucoesMain, instrucoesRotinas, instrucoesFuncoes)
                .flatMap((list) -> Stream.concat(list.stream(), Stream.of("")))
                .collect(Collectors.joining("\n"));
    }

    // Garante que a rotina faça parte do programa, gerando-a uma única vez
    public void incluirRotina(RotinaMIPS32 rotina) {
        if (!rotinasIncluidas.add(rotina)) {
            return;
        }

        ModoGerador modoAnterior = modoAtual;
        modoAtual = ModoGerador.ROTINAS;

        gerarRotina("");
        label(rotina.label);
        switch (rotina) {
            case ERRO_EXECUCAO -> {
                comentario("$a1: endereço da mensagem (bytes UTF-8), $a2: tamanho da mensagem em bytes");
                instrucao("li", RegMIPS32.V0, 15).comentario("syscall para escrever em arquivo");
                instrucao("li", RegMIPS32.A0, 2).comentario("descritor do stderr");
                instrucao("syscall");
                instrucao("li", RegMIPS32.A0, 1).comentario("código de saída de erro");
                instrucao("li", RegMIPS32.V0, 17).comentario("syscall para finalizar o programa");
                instrucao("syscall");
            }
            case ZERA_VETOR -> {
                comentario("$a0: início, $a1: fim (exclusivo), os dois múltiplos de 4");
                comentario(
                        "Zera pelo menos uma palavra antes de testar: a análise semântica proíbe vetor de tamanho 0"
                );
                instrucao("sw", RegMIPS32.ZERO, new Deslocamento(RegMIPS32.A0, 0));
                instrucao("addiu", RegMIPS32.A0, RegMIPS32.A0, 4).comentario("próxima palavra");
                instrucao("bne", RegMIPS32.A0, RegMIPS32.A1, rotina.label)
                        .comentario("repete até chegar ao fim");
                instrucao("jr", RegMIPS32.RA);
            }
            case RAND -> {
                comentario("$a0: limite superior. Resultado em $v0");
                instrucao("blez", RegMIPS32.A0, rotina.label + "_zero")
                        .comentario("limite menor ou igual a 0");
                instrucao("move", RegMIPS32.A1, RegMIPS32.A0).comentario("limite superior em $a1");
                instrucao("li", RegMIPS32.V0, 42)
                        .comentario("syscall para gerar inteiro aleatório em [0, $a1)");
                instrucao("syscall").comentario("resultado em $a0");
                instrucao("move", RegMIPS32.V0, RegMIPS32.A0);
                instrucao("jr", RegMIPS32.RA);
                label(rotina.label + "_zero");
                instrucao("move", RegMIPS32.V0, RegMIPS32.ZERO);
                instrucao("jr", RegMIPS32.RA);
            }
            // Obriga o compilador a exigir um case para cada rotina: sem ele, uma rotina nova geraria só o label,
            // e quem a chamasse cairia no código seguinte
            case null -> throw new BugCompilador("Rotina nula");
        }
        gerarRotina("");

        modoAtual = modoAnterior;
    }
}
