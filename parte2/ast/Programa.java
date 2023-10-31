package src.raiz.ast;

import java.util.ArrayList;
import java.util.List;

public class Programa {

    private final List<Declaracao> declaracoes = new ArrayList<>();
    private final List<String> errosSemanticos = new ArrayList<>();

    public List<Declaracao> getDeclaracoes() {
        return declaracoes;
    }

    public List<String> getErrosSemanticos() {
        return errosSemanticos;
    }

    public void addDeclaracao(Declaracao declaracao) {
        declaracoes.add(declaracao);
    }

    public void reportaErroSemantico(String mensagem) {
        errosSemanticos.add(mensagem);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Declaracao declaracao : declaracoes) {
            sb.append(declaracao.representacaoString()).append("\n");
        }

        return sb.toString();
    }
}
