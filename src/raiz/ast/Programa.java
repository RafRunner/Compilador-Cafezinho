package src.raiz.ast;

import java.util.LinkedList;
import java.util.List;

import src.raiz.util.AstUtil;

// Classe que representa um programa completo. É basicamente uma lista de Declarações
// e possíveis erros semânticos
public class Programa {

    private final List<Declaracao> declaracoes = new LinkedList<>();
    private final List<String> errosSemanticos = new LinkedList<>();

    public List<Declaracao> getDeclaracoes() {
        return declaracoes;
    }

    public List<String> getErrosSemanticos() {
        return errosSemanticos;
    }

    public void reportaErroSemantico(String mensagem) {
        errosSemanticos.add(mensagem);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Programa {\n");
        StringBuilder comIndentacao = new StringBuilder();

        AstUtil.toStrings(sb, this.declaracoes, ", ");

        sb.append("}");
        String[] linhas = sb.toString().split("\n");

        int nivel = 0;
        for (String linha : linhas) {
            for (int j = 0; j < nivel; j++) {
                comIndentacao.append("  ");
            }
            if (linha.trim().endsWith("{")) {
                nivel++;
            } else if (linha.trim().endsWith("}")) {
                nivel--;
            }
            comIndentacao.append(linha).append("\n");
        }

        return comIndentacao.toString();
    }

    public String programaOriginal() {
        StringBuilder sb = new StringBuilder();
        AstUtil.representacoesString(sb, this.declaracoes, "\n");
        return sb.toString();
    }
}
