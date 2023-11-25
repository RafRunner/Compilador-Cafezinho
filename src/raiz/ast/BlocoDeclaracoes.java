package src.raiz.ast;

import src.raiz.token.Token;

import java.util.LinkedList;
import java.util.List;

public class BlocoDeclaracoes extends Declaracao {

    private final List<Declaracao> declaracaoes = new LinkedList<>();

    public BlocoDeclaracoes(Token token) {
        super(token);
    }

    public List<Declaracao> getDeclaracaoes() {
        return declaracaoes;
    }

    @Override
    public String representacaoString() {
        StringBuilder sb = new StringBuilder("{\n");

        int i = 0;
        for (Declaracao declaracao : this.declaracaoes) {
            sb.append(declaracao.representacaoString());
            if (i < this.declaracaoes.size() - 1) {
                sb.append("\n");
            }
            i++;
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "BlocoDeclaracoes\n{" +
                "\ndeclaracaoes=" + declaracaoes +
                "\n}";
    }
}
