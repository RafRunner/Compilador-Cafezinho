package src.raiz.ast.expressoes;

import src.raiz.ast.TipoVariavel;
import src.raiz.token.Token;

// Conversão explícita: <expressão> como <tipo>. O token é o do 'como', para erros apontarem para a conversão
public final class ExpressaoConversao extends Expressao {

    private final Expressao expressao;
    private final TipoVariavel destino;

    public ExpressaoConversao(Token token, Expressao expressao, TipoVariavel destino) {
        super(token);
        this.expressao = expressao;
        this.destino = destino;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    public TipoVariavel getDestino() {
        return destino;
    }

    @Override
    protected String codigoOriginalImpl() {
        return expressao.codigoOriginal() + " como " + destino.getLexema();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.getClass().getSimpleName() + " {\n"
                + getIndentacao(profundidade) + "expressao: " + expressao.representacaoArvore(profundidade + 1) + ",\n"
                + getIndentacao(profundidade) + "destino: " + destino.getLexema() + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}
