package src.raiz.ast;

import java.util.LinkedList;
import java.util.List;

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
        StringBuilder sb = new StringBuilder("Programa[\n");

        int i = 0;
        for (Declaracao declaracao : declaracoes) {
            sb.append(declaracao);
            if (i < declaracoes.size() - 1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("]");
        String[] linhas = sb.toString().split("\n");

        StringBuilder identacao = new StringBuilder();

        int nivel = 0;
        for (String linha : linhas) {
            if (linha.equals("}")) {
                nivel--;
            }
            for (int j = 0; j < nivel; j++) {
                identacao.append("  ");
            }
            if (linha.equals("{")) {
                nivel++;
            }
            identacao.append(linha).append("\n");
        }

        return identacao.toString();
    }

    public String programaOriginal() {
        StringBuilder sb = new StringBuilder();

        for (Declaracao declaracao : declaracoes) {
            sb.append(declaracao.representacaoString()).append("\n");
        }

        return sb.toString();
    }
}
