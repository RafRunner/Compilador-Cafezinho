package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
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
               + getIdentacao(profundidade) + "condicao: " + condicao.representacaoArvore(profundidade + 1) + ",\n"
               + getIdentacao(profundidade) + "se: " + se.representacaoArvore(profundidade + 1) + ",\n"
               + getIdentacao(profundidade) + "senao: " + senao + "\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}
