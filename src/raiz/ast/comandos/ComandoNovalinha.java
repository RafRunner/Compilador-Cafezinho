package src.raiz.ast.comandos;

import src.raiz.token.Token;

public class ComandoNovalinha extends Comando {

    public ComandoNovalinha(Token token) {
        super(token);
    }

    @Override
    public String codigoOriginal() {
        return "novalinha;";
    }

    @Override
    public String toString() {
        return "ComandoNovalinha";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return toString();
    }
}
