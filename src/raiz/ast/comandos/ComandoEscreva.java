package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoEscreva extends Comando {

    private final Expressao expressao;

    public ComandoEscreva(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String representacaoString() {
        return "escreva " + expressao.representacaoString() + ";";
    }

    @Override
    public String toString() {
        return "ComandoEscreva { " + expressao + " }";
    }

}
