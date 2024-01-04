package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoMenor extends ExpressaoBinaria {
    public ExpressaoMenor(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
