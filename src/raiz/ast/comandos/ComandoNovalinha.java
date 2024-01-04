package src.raiz.ast.comandos;

import src.raiz.token.Token;

public class ComandoNovalinha extends Comando {

    public ComandoNovalinha(Token token) {
        super(token);
    }

    @Override
    public String representacaoString() {
        return "novalinha;";
    }

    @Override
    public String toString() {
        return "ComandoNovalinha {}";
    }

}
