package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoCaractereLiteral extends ExpressaoLiteral<Character> {

    public ExpressaoCaractereLiteral(Token token) {
        super(token);
    }

    @Override
    protected Character converter() {
        return getToken().lexema().charAt(1);
    }

    @Override
    public String toString() {
        return "'" + getConteudo() + "'";
    }
}
