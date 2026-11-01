package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.Expressao;
import src.raiz.token.Token;

public final class ComandoSe extends Comando {

    private final Expressao se;
    private final Comando consequencia;
    private final Comando alternativa;

    public ComandoSe(Token token, Expressao se, Comando consequencia, Comando alternativa) {
        super(token);
        this.se = se;
        this.consequencia = consequencia;
        this.alternativa = alternativa;
    }

    public ComandoSe(Token token, Expressao se, Comando consequencia) {
        this(token, se, consequencia, null);
    }

    public Expressao getSe() {
        return se;
    }

    public Comando getConsequencia() {
        return consequencia;
    }

    public Comando getAlternativa() {
        return alternativa;
    }

    @Override
    public String codigoOriginal() {
        String base = "se (" + se.codigoOriginal() + ") entao" + codigoCorpo(consequencia);
        if (alternativa == null) {
            return base;
        }
        // "senao se" fica na mesma linha para uma cadeia de se/senao não ir aumentando a indentação
        if (alternativa instanceof ComandoSe) {
            return base + "\nsenao " + alternativa.codigoOriginal();
        }
        return base + "\nsenao" + codigoCorpo(alternativa);
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoSe {\n"
                + getIndentacao(profundidade) + "se: " + se.representacaoArvore(profundidade + 1) + ",\n"
                + getIndentacao(profundidade) + "consequencia: " + consequencia.representacaoArvore(profundidade + 1)
                + ",\n"
                + getIndentacao(profundidade) + "alternativa: "
                + (alternativa != null ? alternativa.representacaoArvore(profundidade + 1) : "null") + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}
