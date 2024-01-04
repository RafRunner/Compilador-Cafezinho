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
    public String representacaoString() {
        String base = "se (" + se.representacaoString() + ")\nentao\n" + consequencia.representacaoString();
        if (alternativa == null) {
            return base;
        }
        return base + "\nsenao\n" + alternativa.representacaoString();
    }

    @Override
    public String toString() {
        return "ComandoSe { se=" + se + ", consequencia=" + consequencia + ", alternativa=" + alternativa + " }";
    }

}
