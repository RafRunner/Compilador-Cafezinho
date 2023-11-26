package src.raiz.ast.comandos;

import src.raiz.ast.Declaracao;
import src.raiz.token.Token;

public abstract class Comando extends Declaracao {

    public Comando(Token token) {
        super(token);
    }
}
