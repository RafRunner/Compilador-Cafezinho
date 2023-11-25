package src.raiz.ast;

import src.raiz.token.Token;

public abstract class NoSintatico {

    private Token token;

    public NoSintatico(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    protected void setToken(Token token) {
        this.token = token;
    }

    public abstract String representacaoString();
}
