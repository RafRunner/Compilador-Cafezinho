package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.token.Token;

public class ComandoLeia extends Comando {

    private final ExpressaoIdentificador expressaoIdentificador;

    public ComandoLeia(Token token, ExpressaoIdentificador expressaoIdentificador) {
        super(token);
        this.expressaoIdentificador = expressaoIdentificador;
    }

    public ExpressaoIdentificador getExpressaoIdentificador() {
        return expressaoIdentificador;
    }

    @Override
    public String representacaoString() {
        return "leia " + expressaoIdentificador.representacaoString() + ";";
    }

    @Override
    public String toString() {
        return "ComandoLeia { " + expressaoIdentificador + " }";
    }

}
