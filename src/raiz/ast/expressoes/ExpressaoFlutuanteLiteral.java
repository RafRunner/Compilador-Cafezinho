package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoFlutuanteLiteral extends ExpressaoLiteral<Float> {

    public ExpressaoFlutuanteLiteral(Token token) {
        super(token);
    }

    @Override
    protected Float converter() {
        return Float.parseFloat(getToken().getLexema());
    }
}
