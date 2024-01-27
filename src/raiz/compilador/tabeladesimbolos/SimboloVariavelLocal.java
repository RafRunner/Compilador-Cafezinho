package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.Variavel;

public class SimboloVariavelLocal extends Simbolo<Variavel> {

    private final int offset; // offset em relação ao stack pointer

    public SimboloVariavelLocal(Variavel noSintatico, int offset) {
        super(noSintatico, TipoSimbolo.VARIAVEL_LOCAL);
        this.offset = offset;
    }

    public VariavelNoStack getVariavelNoStack() {
        return new VariavelNoStack(offset, getNoSintatico().getTipo().getTipo(), getNoSintatico().getNome(), getNoSintatico().isVetor());
    }

    @Override
    public TipoVariavel getTipoVariavel() {
        return getNoSintatico().getTipo().getTipo();
    }

    @Override
    public boolean isVetor() {
        return getNoSintatico().isVetor();
    }
}
