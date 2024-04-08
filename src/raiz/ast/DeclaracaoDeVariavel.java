package src.raiz.ast;

import java.util.List;
import java.util.stream.Collectors;

// Declaração de uma ou mais variáveis. Ex: int ano, mes, dias[];
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
    public String codigoOriginal() {
        return tipo.codigoOriginal() + " "
               + variaveis.stream().map(Variavel::declaracaoSemTipo).collect(Collectors.joining(", "))
               + ";";
    }

    @Override
    public String toString() {
        return "DeclaracaoDeVariavel {\ntipo=" + tipo + ",\nvariaveis=" + variaveis + "\n}\n";
    }

}
