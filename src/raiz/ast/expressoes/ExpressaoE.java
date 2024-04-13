package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoE extends ExpressaoBinaria {

    public ExpressaoE(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
