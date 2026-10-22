package src.raiz.ast.comandos;

import src.raiz.token.Token;

// O comando ';' sozinho, que não faz nada. Ex: se (x) entao ; senao escreva 1;
public final class ComandoVazio extends Comando {

    public ComandoVazio(Token token) {
        super(token);
    }

    @Override
    public String codigoOriginal() {
        return ";";
    }

    @Override
    public String toString() {
        return "ComandoVazio {}";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return toString();
    }
}
