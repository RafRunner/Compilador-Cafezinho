package src.raiz.compilador.mips32;

import src.raiz.compilador.GeradorDeCodigo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Gerador de código específico para a arquitetura MIPS 32
public class GeradorDeCodigoMIPS32 extends GeradorDeCodigo {

    private final Set<RotinaMIPS32> rotinasIncluidas = EnumSet.noneOf(RotinaMIPS32.class);

    public GeradorDeCodigoMIPS32() {
        // Iniciando seção de variáveis globais
        gerarVarGlobal(".data");

        // Iniciando seção de código
        gerarMain(".text");
    }

    @Override
    public String geraCodigoObjeto() {
        return Stream.of(instrucoesVariaveisGlobais, instrucoesMain, instrucoesFuncoes)
                .flatMap((list) -> Stream.concat(list.stream(), Stream.of("")))
                .collect(Collectors.joining("\n"));
    }

    // Garante que a rotina faça parte do programa, gerando-a uma única vez
    public void incluirRotina(RotinaMIPS32 rotina) {
        if (!rotinasIncluidas.add(rotina)) {
            return;
        }

        // As rotinas ficam antes das funções do programa
        List<String> funcoesAteAgora = new ArrayList<>(instrucoesFuncoes);
        instrucoesFuncoes.clear();

        gerarFuncao("");
        gerarFuncao(rotina.label + ":");
        switch (rotina) {
            case ERRO_EXECUCAO -> {
                gerarFuncao("# $a1: endereço da mensagem (bytes UTF-8), $a2: tamanho da mensagem em bytes");
                gerarFuncao("li    $v0, 15 # syscall para escrever em arquivo");
                gerarFuncao("li    $a0, 2  # descritor do stderr");
                gerarFuncao("syscall");
                gerarFuncao("li    $a0, 1  # código de saída de erro");
                gerarFuncao("li    $v0, 17 # syscall para finalizar o programa com código de retorno");
                gerarFuncao("syscall");
            }
            case RAND -> {
                gerarFuncao("# $a0: limite superior. Resultado em $v0");
                gerarFuncao("blez  $a0, " + rotina.label + "_zero # limite menor ou igual a 0");
                gerarFuncao("move  $a1, $a0   # limite superior em $a1");
                gerarFuncao("li    $v0, 42    # syscall para gerar inteiro aleatório em [0, $a1)");
                gerarFuncao("syscall          # resultado em $a0");
                gerarFuncao("move  $v0, $a0");
                gerarFuncao("jr    $ra");
                gerarFuncao(rotina.label + "_zero:");
                gerarFuncao("move  $v0, $zero");
                gerarFuncao("jr    $ra");
            }
        }
        gerarFuncao("");

        instrucoesFuncoes.addAll(funcoesAteAgora);
    }
}
