package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoRetorno extends Comando {

    private final Expressao expressao;

    public ComandoRetorno(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String representacaoString() {
        return "retorne " + expressao.representacaoString() + ";";
    }

    @Override
    public String toString() {
        return "ComandoRetorno { " + expressao + " }";
    }

}
