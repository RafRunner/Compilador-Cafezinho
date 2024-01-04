package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public abstract class ExpressaoUnaria extends Expressao {

    private final Expressao expressao;
    private final String operacao;

    public ExpressaoUnaria( Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
        this.operacao = token.getLexema();
    }

    public Expressao getExpressao() {
        return expressao;
    }

    public String getOperacao() {
        return operacao;
    }

    @Override
    public String representacaoString() {
        return operacao + expressao.representacaoString();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "expressao=" + expressao +
                ", operacao='" + operacao + '\'' +
                " }";
    }
}
