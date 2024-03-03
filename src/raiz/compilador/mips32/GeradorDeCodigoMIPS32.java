package src.raiz.compilador.mips32;

import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.ModoGerador;

import java.util.ArrayList;
import java.util.List;

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
        StringBuilder sb = new StringBuilder();

        List<String> todas = new ArrayList<>(instrucoesVariaveisGlobais);
        todas.add("");
        todas.addAll(instrucoesMain);
        todas.add("");
        todas.addAll(instrucoesFuncoes);

        for (String instrucao : todas) {
            sb.append(instrucao).append("\n");
        }

        return sb.toString();
    }

}
