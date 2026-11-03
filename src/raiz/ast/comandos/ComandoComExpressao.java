package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;

public final class ComandoComExpressao extends Comando {

    private final Expressao expressao;

    public ComandoComExpressao(Expressao expressao) {
        super(expressao.getToken());
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String codigoOriginal() {
        String expressao = this.expressao.codigoOriginal();

        if (!expressao.endsWith(";")) {
            expressao += ";";
        }

        return expressao;
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoComExpressao {\n"
                + getIndentacao(profundidade) + expressao.representacaoArvore(profundidade + 1) + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}