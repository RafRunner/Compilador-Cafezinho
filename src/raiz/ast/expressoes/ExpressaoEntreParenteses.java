package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoEntreParenteses extends Expressao {

    private final Expressao expressao;

    public ExpressaoEntreParenteses(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String representacaoString() {
        return "(" + expressao.representacaoString() + ")";
    }

    @Override
    public String toString() {
        return "ExpressaoEntreParenteses {" +
                "expressao=" + expressao +
                " }";
    }
}
