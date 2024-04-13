package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoDiferente extends ExpressaoBinaria {

    public ExpressaoDiferente(Token token, Expressao esquerda, Expressao direita) {
        super(token, esquerda, direita);
    }
}
