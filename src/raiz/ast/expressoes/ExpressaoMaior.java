package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoMaior extends ExpressaoBinaria {
    public ExpressaoMaior(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
