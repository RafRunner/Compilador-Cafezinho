package src.raiz.ast.artificiais;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.expressoes.Expressao;
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
        return "expressão interna";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ExpressaoLeia { " + tipoVariavel + " }";
    }
}
