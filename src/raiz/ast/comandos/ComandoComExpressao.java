package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoComExpressao extends Comando {

    private final Expressao expressao;

    public ComandoComExpressao(Token token, Expressao expressao) {
        super(token);
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
               + getIdentacao(profundidade) + expressao.representacaoArvore(profundidade + 1) + "\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}