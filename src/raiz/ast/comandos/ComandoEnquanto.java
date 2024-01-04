package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoEnquanto extends Comando {

    private final Expressao condicional;
    private final Comando comando;

    public ComandoEnquanto(Token token, Expressao condicional, Comando comando) {
        super(token);
        this.condicional = condicional;
        this.comando = comando;
    }

    public Expressao getCondicional() {
        return condicional;
    }

    public Comando getComando() {
        return comando;
    }

    @Override
    public String representacaoString() {
        return "enquanto (" + condicional.representacaoString() + ") execute {\n" + comando.representacaoString()
                + "\n}";
    }

    @Override
    public String toString() {
        return "ComandoEnquanto { condicional=" + condicional + ", comando=" + comando + " }";
    }

}
