package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.Variavel;

public class SimboloVariavelLocal extends Simbolo<Variavel> {

    private final int offset; // offset em relação ao stack pointer

    public SimboloVariavelLocal(Variavel noSintatico, int offset) {
        super(noSintatico, TipoSimbolo.VARIAVEL_LOCAL);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
