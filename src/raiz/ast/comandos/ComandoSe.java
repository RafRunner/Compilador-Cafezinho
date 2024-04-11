package src.raiz.ast.comandos;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;

public class ComandoSe extends Comando {

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
        String base = "se (" + se.codigoOriginal() + ")\nentao\n" + consequencia.codigoOriginal();
        if (alternativa == null) {
            return base;
        }
        return base + "\nsenao\n" + alternativa.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoSe {\n"
               + getIdentacao(profundidade) + "se: " + se.representacaoArvore(profundidade + 1) + ",\n"
               + getIdentacao(profundidade) + "consequencia: " + consequencia.representacaoArvore(profundidade + 1) + ",\n"
               + getIdentacao(profundidade) + "alternativa: " + (alternativa != null ? alternativa.representacaoArvore(profundidade + 1) : "null") + "\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}
