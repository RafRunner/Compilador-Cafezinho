package src.raiz.ast;

import src.raiz.token.Token;

import java.util.ArrayList;
import java.util.List;

public class BlocoDeclaracoes extends Declaracao {

    private List<Declaracao> declaracaoes = new ArrayList<>();

    public BlocoDeclaracoes(Token token) {
        super(token);
    }

    public List<Declaracao> getDeclaracaoes() {
        return declaracaoes;
    }

    @Override
    public String representacaoString() {
        StringBuilder sb = new StringBuilder("{\n");

        for (Declaracao declaracao : this.declaracaoes) {
            sb.append(declaracao.representacaoString()).append("\n");
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
