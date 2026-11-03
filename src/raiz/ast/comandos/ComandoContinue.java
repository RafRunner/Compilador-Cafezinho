package src.raiz.ast.comandos;

import src.raiz.token.Token;

public final class ComandoContinue extends Comando {

    private ComandoEnquanto enquanto;

    public ComandoContinue(Token token) {
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
        return "continue;";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoContinue {}";
    }
}
