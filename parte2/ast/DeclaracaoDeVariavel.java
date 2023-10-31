package src.raiz.ast;

import java.util.List;

public class DeclaracaoDeVariavel extends Declaracao {

    private final TipoVariavelNo tipo;
    private final List<Variavel> variaveis;

    public DeclaracaoDeVariavel(TipoVariavelNo tipo, List<Variavel> variaveis) {
        super(tipo.getToken());
        this.tipo = tipo;
        this.variaveis = variaveis;
    }

    public TipoVariavelNo getTipo() {
        return tipo;
    }

    public List<Variavel> getVariaveis() {
        return variaveis;
    }

    @Override
    public String representacaoString() {
        StringBuilder sb = new StringBuilder();
        sb.append(tipo.representacaoString()).append(" ");

        int i = 0;
        for (Variavel variavel : variaveis) {
            sb.append(variavel.declaracaoSemTipo());
            if (i < variaveis.size() - 1) {
                sb.append(", ");
            } else {
                sb.append(";");
            }
            i++;
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "DeclaracaoDeVariavel [tipo=" + tipo + ", variaveis=" + variaveis + "]";
    }

}
