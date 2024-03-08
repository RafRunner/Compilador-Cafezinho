package src.raiz.compilador;

import src.raiz.ast.TipoVariavel;

public class ParametroFuncaoNativa {

    private final TipoVariavel tipo;
    private final String nome;
    private final boolean vetor;

    public ParametroFuncaoNativa(TipoVariavel tipo, String nome, boolean vetor) {
        this.tipo = tipo;
        this.nome = nome;
        this.vetor = vetor;
    }

    public ParametroFuncaoNativa(TipoVariavel tipo, String nome) {
        this(tipo, nome, false);
    }

    public TipoVariavel getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isVetor() {
        return vetor;
    }
}
