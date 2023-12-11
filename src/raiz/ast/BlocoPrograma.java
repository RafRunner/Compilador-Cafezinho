package src.raiz.ast;

import src.raiz.token.Token;

public class BlocoPrograma extends Declaracao {

    private final BlocoDeclaracoes blocoDeclaracoes;

    public BlocoPrograma(Token token, BlocoDeclaracoes blocoDeclaracoes) {
        super(token);
        this.blocoDeclaracoes = blocoDeclaracoes;
    }

    public BlocoDeclaracoes getBlocoDeclaracoes() {
        return blocoDeclaracoes;
    }

    @Override
    public String representacaoString() {
        return "programa" + blocoDeclaracoes.representacaoString();
    }

    @Override
    public String toString() {
        return "BlocoPrograma {\n" +
                "blocoDeclaracoes=" + blocoDeclaracoes +
                "\n}\n";
    }
}
