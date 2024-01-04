package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoTernaria extends Expressao {

    private final Expressao condicao;
    private final Expressao se;
    private final Expressao senao;

    public ExpressaoTernaria(Token token, Expressao condicao, Expressao se, Expressao senao) {
        super(token);
        this.condicao = condicao;
        this.se = se;
        this.senao = senao;
    }

    public Expressao getCondicao() {
        return condicao;
    }

    public Expressao getSe() {
        return se;
    }

    public Expressao getSenao() {
        return senao;
    }

    @Override
    public String representacaoString() {
        return condicao.representacaoString() + " ? " + se.representacaoString() + " : " + senao.representacaoString() + ";";
    }

    @Override
    public String toString() {
        return "ExpressaoTernaria {" +
                "condicao=" + condicao +
                ", se=" + se +
                ", senao=" + senao +
                " }";
    }
}
