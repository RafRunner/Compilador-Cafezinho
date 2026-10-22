package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoUnaria extends Expressao {

    private final OperadorUnario operador;
    private final Expressao expressao;

    public ExpressaoUnaria(Token token, OperadorUnario operador, Expressao expressao) {
        super(token);
        this.operador = operador;
        this.expressao = expressao;
    }

    public OperadorUnario getOperador() {
        return operador;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String codigoOriginal() {
        return operador.getLexema() + expressao.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.getClass().getSimpleName() + " {\n"
               + getIndentacao(profundidade) + "expressao: " + expressao.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "operador: " + operador + " '" + operador.getLexema() + "'\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
