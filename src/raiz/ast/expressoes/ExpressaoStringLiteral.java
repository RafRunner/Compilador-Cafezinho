package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoStringLiteral extends ExpressaoLiteral<String> {

    public ExpressaoStringLiteral(Token token) {
        super(token);
    }

    @Override
    protected String converter() {
        return this.getToken().lexema();
    }

    @Override
    public String toString() {
        return '"' + super.toString() + '"';
    }
}
