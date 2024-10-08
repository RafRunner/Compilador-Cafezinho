package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;
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
    public String codigoOriginal() {
        return "enquanto (" + condicional.codigoOriginal() + ") execute \n" + comando.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoEnquanto {\n"
               + getIndentacao(profundidade) + "condicional: " + condicional.representacaoArvore(profundidade + 1) + ",\n"
               + getIndentacao(profundidade) + "comando: " + comando.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
