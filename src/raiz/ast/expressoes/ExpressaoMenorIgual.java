package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoMenorIgual extends ExpressaoBinaria {
    public ExpressaoMenorIgual(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
