package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoIgual extends ExpressaoBinaria {

    public ExpressaoIgual(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
