package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;
import src.raiz.token.Token;

public final class ComandoEnquanto extends Comando {

    private final Expressao condicional;
    private final Comando comando;
    // Só existe nos laços que vêm de um para: roda depois do corpo, a cada volta
    private final ComandoComExpressao passo;

    public ComandoEnquanto(Token token, Expressao condicional, Comando comando) {
        this(token, condicional, comando, null);
    }

    public ComandoEnquanto(Token token, Expressao condicional, Comando comando, ComandoComExpressao passo) {
        super(token);
        this.condicional = condicional;
        this.comando = comando;
        this.passo = passo;
    }

    public Expressao getCondicional() {
        return condicional;
    }

    public Comando getComando() {
        return comando;
    }

    public ComandoComExpressao getPasso() {
        return passo;
    }

    @Override
    public String codigoOriginal() {
        if (passo == null) {
            return "enquanto (" + condicional.codigoOriginal() + ") execute" + codigoCorpo(comando);
        }
        return "para (; " + condicional.codigoOriginal() + "; " + passo.getExpressao().codigoOriginal() + ") execute"
                + codigoCorpo(comando);
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoEnquanto {\n"
                + getIndentacao(profundidade) + "condicional: " + condicional.representacaoArvore(profundidade + 1)
                + ",\n"
                + getIndentacao(profundidade) + "comando: " + comando.representacaoArvore(profundidade + 1) + "\n"
                + (passo == null
                        ? ""
                        : getIndentacao(profundidade) + "passo: " + passo.representacaoArvore(profundidade + 1) + "\n")
                + getIndentacao(profundidade - 1) + "}";
    }
}
