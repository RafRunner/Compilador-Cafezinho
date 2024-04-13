package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoNegacao extends ExpressaoUnaria {
    public ExpressaoNegacao(Token token, Expressao expressao) {
        super(token, expressao);
    }
}
