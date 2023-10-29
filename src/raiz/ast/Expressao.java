package src.raiz.ast;

import src.raiz.token.Token;

public abstract class Expressao extends NoSintatico {

    public Expressao(Token token) {
        super(token);
    }
}
