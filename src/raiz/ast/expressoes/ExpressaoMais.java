package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoMais extends ExpressaoBinaria {
    public ExpressaoMais(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
