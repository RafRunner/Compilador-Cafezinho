package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public abstract class ExpressaoBinaria extends Expressao {

    private final Expressao esquerda;
    private final Expressao direita;
    private final String operacao;

    public ExpressaoBinaria(Token token, Expressao esquerda, Expressao direita) {
        super(token);
        this.operacao = token.lexema();
        this.esquerda = esquerda;
        this.direita = direita;
    }

    public Expressao getEsquerda() {
        return esquerda;
    }

    public Expressao getDireita() {
        return direita;
    }

    public String getOperacao() {
        return operacao;
    }

    @Override
    public String codigoOriginal() {
        return esquerda.codigoOriginal() + " " + operacao + " " + direita.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.getClass().getSimpleName() + " {\n"
               + getIndentacao(profundidade) + "esquerda: " + esquerda.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "operacao: '" + operacao + "',\n"
               + getIndentacao(profundidade) + "direita: " + direita.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
