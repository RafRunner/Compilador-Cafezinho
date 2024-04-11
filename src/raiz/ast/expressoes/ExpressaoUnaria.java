package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public abstract class ExpressaoUnaria extends Expressao {

    private final Expressao expressao;
    private final String operacao;

    public ExpressaoUnaria(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
        this.operacao = token.lexema();
    }

    public Expressao getExpressao() {
        return expressao;
    }

    public String getOperacao() {
        return operacao;
    }

    @Override
    public String codigoOriginal() {
        return operacao + expressao.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.getClass().getSimpleName() + " {\n"
               + getIdentacao(profundidade) + "expressao: " + expressao.representacaoArvore(profundidade + 1) + ",\n"
               + getIdentacao(profundidade) + "operacao: '" + operacao + "'\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}
