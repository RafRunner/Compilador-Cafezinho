package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;
import src.raiz.token.Token;

public class ComandoEscreva extends Comando {

    private final Expressao expressao;

    public ComandoEscreva(Token token, Expressao expressao) {
        super(token);
        this.expressao = expressao;
    }

    public Expressao getExpressao() {
        return expressao;
    }

    @Override
    public String codigoOriginal() {
        return "escreva " + expressao.codigoOriginal() + ";";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoEscreva {\n"
               + getIndentacao(profundidade) + expressao.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
