package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoResto extends ExpressaoBinaria {
    public ExpressaoResto(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
