package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoOu extends ExpressaoBinaria {

    public ExpressaoOu(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
