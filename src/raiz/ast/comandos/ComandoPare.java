package src.raiz.ast.comandos;

import src.raiz.token.Token;

public final class ComandoPare extends Comando {

    private ComandoEnquanto enquanto;

    public ComandoPare(Token token) {
        super(token);
    }

    public ComandoEnquanto getEnquanto() {
        return enquanto;
    }

    public void setEnquanto(ComandoEnquanto enquanto) {
        this.enquanto = enquanto;
    }

    @Override
    public String codigoOriginal() {
        return "pare;";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoPare {}";
    }
}
