package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoComExpressao extends Comando {

    private final Expressao expressao;

    public ComandoComExpressao(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String representacaoString() {
        return expressao.representacaoString();
    }

    @Override
    public String toString() {
        return "ComandoComExpressao { " + expressao + " }";
    }

}