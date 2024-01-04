package src.raiz.ast.expressoes;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public abstract class ExpressaoBinaria extends Expressao {

    private final Expressao esquerda;
    private final Expressao direita;
    private final String operacao;

    public ExpressaoBinaria(Token token, Expressao esquerda, Expressao direita) {
        super(token);
        this.operacao = token.getLexema();
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
    public String representacaoString() {
        return esquerda.representacaoString() + " " + operacao + " " + direita.representacaoString();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "esquerda=" + esquerda +
                ", operacao='" + operacao + '\'' +
                ", direita=" + direita +
                " }";
    }
}
