package src.raiz.compilador;

import java.util.ArrayList;
import java.util.List;

// Classe responsável por acumular as instruções que formarão o programa objeto
public abstract class GeradorDeCodigo {

    protected final List<String> instrucoesVariaveisGlobais = new ArrayList<>();
    protected final List<String> instrucoesMain = new ArrayList<>();
    protected final List<String> instrucoesFuncoes = new ArrayList<>();
    protected ModoGerador modoAtual = ModoGerador.MAIN;

    public void setModoAtual(ModoGerador modoAtual) {
        this.modoAtual = modoAtual;
    }

    public void gerar(String instrucao) {
        if (modoAtual == ModoGerador.FUNCAO) {
            gerarFuncao(instrucao);
        } else if (modoAtual == ModoGerador.VARIAVEIS_GLOBAIS) {
            gerarVarGlobal(instrucao);
        } else if (modoAtual == ModoGerador.MAIN) {
            gerarMain(instrucao);
        }
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

    public abstract String geraCodigoObjeto();
}
