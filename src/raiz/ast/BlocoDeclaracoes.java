package src.raiz.ast;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

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
        AstUtil.representacoesString(sb, this.declaracaoes, "\n");
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
