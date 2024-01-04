package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoNegativo extends ExpressaoUnaria {
    public ExpressaoNegativo(Token token, Expressao expressao) {
        super(token, expressao);
    }
}
