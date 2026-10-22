package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public abstract sealed class ExpressaoLiteral<T> extends Expressao
        permits ExpressaoInteiroLiteral, ExpressaoFlutuanteLiteral, ExpressaoCaractereLiteral, ExpressaoStringLiteral {

    private final T conteudo;

    public ExpressaoLiteral(Token token) {
        super(token);
        this.conteudo = converter();
    }

    protected abstract T converter();

    public T getConteudo() {
        return conteudo;
    }

    @Override
    public String codigoOriginal() {
        return this.toString();
    }

    @Override
    public String toString() {
        return conteudo.toString();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return toString();
    }
}