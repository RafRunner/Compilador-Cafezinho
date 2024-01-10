package src.raiz.compilador;

import java.util.ArrayList;
import java.util.List;

// Classe responsável por acumular as instruções que formarão o programa objeto
public class GeradorDeCodigo {

    private final List<String> instrucoesVariaveisGlobais = new ArrayList<>();
    private final List<String> instrucoesMain = new ArrayList<>();
    private final List<String> instrucoesFuncoes = new ArrayList<>();

    public GeradorDeCodigo() {
        // Iniciando seção de variáveis globais
        gerarVarGlobal(".data");

        // Iniciando seção de código
        gerarMain(".text");
    }

    public void gerarVarGlobal(String instrucao) {
        instrucoesVariaveisGlobais.add(instrucao);
    }

    public void gerarMain(String instrucao) {
        instrucoesMain.add(instrucao);
    }

    public void gerarFuncao(String instrucao) {
        instrucoesFuncoes.add(instrucao);
    }

    public String codigoObjeto() {
        StringBuilder sb = new StringBuilder();

        List<String> todas = new ArrayList<>();
        todas.addAll(instrucoesVariaveisGlobais);
        todas.addAll(instrucoesMain);
        todas.addAll(instrucoesFuncoes);

        for (String instrucao : todas) {
            sb.append(instrucao).append("\n");
        }

        return sb.toString();
    }

}
