package src.raiz.ast;

import src.raiz.token.Token;

// Tudo que faz parte da Árvore Sintática é um nó sintático
public abstract class NoSintatico {

    // Tudo que é um nó sintátio tem um token. Isso serve para ligar a estrutura com
    // uma localização no código (linha e coluna), um lexema e um tipo.
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

    // A representacaoString é a reconstrução aproximada do código original que
    // levou a esse NoSintatico
    public abstract String representacaoString();
}
