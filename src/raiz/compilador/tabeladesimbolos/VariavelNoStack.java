package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.TipoVariavel;

public class VariavelNoStack {

    private final int offset;
    private final TipoVariavel tipoVariavel;
    private final String nome;
    private final boolean isVetor;

    public VariavelNoStack(int offset, TipoVariavel tipoVariavel, String nome, boolean isVetor) {
        this.offset = offset;
        this.tipoVariavel = tipoVariavel;
        this.nome = nome;
        this.isVetor = isVetor;
    }

    public int getOffset() {
        return offset;
    }

    public TipoVariavel getTipoVariavel() {
        return tipoVariavel;
    }

    public String getNome() {
        return nome;
    }

    public boolean isVetor() {
        return isVetor;
    }
}
