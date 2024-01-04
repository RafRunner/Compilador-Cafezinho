package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoDivisao extends ExpressaoBinaria {
    public ExpressaoDivisao(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
