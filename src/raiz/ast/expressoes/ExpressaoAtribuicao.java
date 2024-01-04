package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoAtribuicao extends Expressao {

    private final ExpressaoIdentificador identificador;
    private final Expressao expressaoLadoDireito;

    public ExpressaoAtribuicao(Token token, ExpressaoIdentificador identificador, Expressao expressaoLadoDireito) {
        super(token);
        this.identificador = identificador;
        this.expressaoLadoDireito = expressaoLadoDireito;
    }

    public ExpressaoIdentificador getIdentificador() {
        return identificador;
    }

    public Expressao getExpressaoLadoDireito() {
        return expressaoLadoDireito;
    }

    @Override
    public String representacaoString() {
        String ladoDireito = expressaoLadoDireito.representacaoString();
        if (!ladoDireito.endsWith(";")) {
            ladoDireito += ";";

        }
        return identificador.representacaoString() + " = " + ladoDireito;
    }

    @Override
    public String toString() {
        return "ExpressaoAtribuicao { " +
                "identificador=" + identificador +
                ", expressaoLadoDireito=" + expressaoLadoDireito +
                " }";
    }
}
