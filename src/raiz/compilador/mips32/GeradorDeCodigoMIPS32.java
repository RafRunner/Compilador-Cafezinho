package src.raiz.compilador.mips32;

import src.raiz.compilador.FuncoesNativas;
import src.raiz.compilador.GeradorDeCodigo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Gerador de código específico para a arquitetura MIPS 32
public class GeradorDeCodigoMIPS32 extends GeradorDeCodigo {

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

    @Override
    protected void gerarCodigoFuncaoNativa(FuncoesNativas funcaNativa) {
        List<String> funcoesAteAgora = new ArrayList<>(instrucoesFuncoes);
        instrucoesFuncoes.clear();

        gerarFuncao("");
        switch (funcaNativa) {
            case RAND -> {
                gerarFuncao("rand:");
                gerarFuncao("# checa se o argumento é menor ou igual a 0");
                gerarFuncao("blez $a0, return_zero");

                gerarFuncao("li   $v0, 42    # syscall para gerar uma integer aleatória");
                gerarFuncao("move $a1, $a0   # move o primeiro argumento para $a1");
                gerarFuncao("syscall         # gera o número aleatório em $a0, entre [0, $a1)");

                gerarFuncao("move $v0, $a0");
                gerarFuncao("jr   $ra        # retorna");

                gerarFuncao("return_zero:");
                gerarFuncao("move $v0, $zero # setta o valor de retorno para 0");
                gerarFuncao("jr   $ra        # retorna");
            }
            case PISO -> {
                gerarFuncao("piso:");
                gerarFuncao("cvt.w.s $f0, $f12 # converte o ponto flutuante em $f12 para um inteiro em $f0");
                gerarFuncao("mfc1    $v0, $f0  # move o conteúdo de $f0 (que é agora um inteiro) para $v0");
                gerarFuncao("jr      $ra       # retorna");
            }
        }
        gerarFuncao("");

        instrucoesFuncoes.addAll(funcoesAteAgora);
    }

}
