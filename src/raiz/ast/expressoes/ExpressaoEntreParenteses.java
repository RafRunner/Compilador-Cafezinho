package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ExpressaoEntreParenteses extends Expressao {

    private final Expressao expressao;

    public ExpressaoEntreParenteses(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String codigoOriginal() {
        return "(" + expressao.codigoOriginal() + ")";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ExpressaoEntreParenteses {\n"
               + getIndentacao(profundidade) + expressao.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + " }";
    }
}
