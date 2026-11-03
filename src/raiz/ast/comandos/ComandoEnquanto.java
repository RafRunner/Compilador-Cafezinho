package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

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
        String cabecalho = "enquanto (" + condicional.codigoOriginal() + ") execute";
        if (passo == null) {
            return cabecalho + codigoCorpo(comando);
        }
        // Enquanto não houver continue, o passo equivale a um comando no fim do corpo
        return cabecalho + " {\n" + AstUtil.indenta(comando.codigoOriginal() + "\n" + passo.codigoOriginal()) + "\n}";
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
