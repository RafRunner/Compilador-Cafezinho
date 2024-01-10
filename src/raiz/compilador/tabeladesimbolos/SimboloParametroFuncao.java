package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.ParametroFuncao;

public class SimboloParametroFuncao extends Simbolo<ParametroFuncao> {

    private final int offset; // offset em relação ao stack pointer

    public SimboloParametroFuncao(ParametroFuncao noSintatico, int offset) {
        super(noSintatico, TipoSimbolo.PARAMETRO_FUNCAO);
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
