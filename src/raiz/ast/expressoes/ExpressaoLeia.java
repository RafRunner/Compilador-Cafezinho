package src.raiz.ast.expressoes;

import src.raiz.ast.TipoVariavel;
import src.raiz.token.Token;

// Não é uma expressão real da gramática, mas existe para facilitar a geração de código do ComandoEscreva, reaproveitando
// a lógica de atribuição
public class ExpressaoLeia extends Expressao {

    private final TipoVariavel tipoVariavel;

    public ExpressaoLeia(Token token, TipoVariavel tipoVariavel) {
        super(token);
        this.tipoVariavel = tipoVariavel;
    }

    public TipoVariavel getTipoVariavel() {
        return tipoVariavel;
    }

    @Override
    public String codigoOriginal() {
        return "ExpressaoLeia { " + tipoVariavel + " }";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "expressão interna";
    }
}
