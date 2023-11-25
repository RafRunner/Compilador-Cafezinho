package src.raiz.ast;

import src.raiz.token.Token;

public abstract class NoSintatico {

    private final Token token;

    public NoSintatico(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public abstract String representacaoString();
}
