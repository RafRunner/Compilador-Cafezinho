package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public final class ExpressaoInteiroLiteral extends ExpressaoLiteral<Integer> {

    public ExpressaoInteiroLiteral(Token token) {
        super(token);
    }

    @Override
    protected Integer converter() {
        return Integer.parseInt(getToken().lexema());
    }
}
