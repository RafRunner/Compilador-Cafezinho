package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoTernaria extends Expressao {

    private final Expressao condicao;
    private final Expressao se;
    private final Expressao senao;

    public ExpressaoTernaria(Token token, Expressao condicao, Expressao se, Expressao senao) {
        super(token);
        this.condicao = condicao;
        this.se = se;
        this.senao = senao;
    }

    public Expressao getCondicao() {
        return condicao;
    }

    public Expressao getSe() {
        return se;
    }

    public Expressao getSenao() {
        return senao;
    }

    @Override
    public String codigoOriginal() {
        return condicao.codigoOriginal() + " ? " + se.codigoOriginal() + " : " + senao.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ExpressaoTernaria {\n"
               + getIndentacao(profundidade) + "condicao: " + condicao.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "se: " + se.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "senao: " + senao + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
