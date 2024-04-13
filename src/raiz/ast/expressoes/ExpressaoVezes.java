package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoVezes extends ExpressaoBinaria {
    public ExpressaoVezes(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
