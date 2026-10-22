package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public final class ExpressaoBinaria extends Expressao {

    private final OperadorBinario operador;
    private final Expressao esquerda;
    private final Expressao direita;

    public ExpressaoBinaria(Token token, OperadorBinario operador, Expressao esquerda, Expressao direita) {
        super(token);
        this.operador = operador;
        this.esquerda = esquerda;
        this.direita = direita;
    }

    public OperadorBinario getOperador() {
        return operador;
    }

    public Expressao getEsquerda() {
        return esquerda;
    }

    public Expressao getDireita() {
        return direita;
    }

    @Override
    public String codigoOriginal() {
        return esquerda.codigoOriginal() + " " + operador.getLexema() + " " + direita.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.getClass().getSimpleName() + " {\n"
               + getIndentacao(profundidade) + "esquerda: " + esquerda.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "operador: " + operador + " '" + operador.getLexema() + "',\n"
               + getIndentacao(profundidade) + "direita: " + direita.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
