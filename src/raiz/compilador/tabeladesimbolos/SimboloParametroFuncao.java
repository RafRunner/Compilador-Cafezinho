package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.TipoVariavel;

public class SimboloParametroFuncao extends Simbolo<ParametroFuncao> {

    private final int offset; // offset em relação ao stack pointer

    public SimboloParametroFuncao(ParametroFuncao noSintatico, int offset) {
        super(noSintatico, TipoSimbolo.PARAMETRO_FUNCAO);
        this.offset = offset;
    }

    public VariavelNoStack getVariavelNoStack() {
        return new VariavelNoStack(offset, getNoSintatico().getTipo().getTipo(), getNoSintatico().getNome(), getNoSintatico().isVetor());
    }

    @Override
    public TipoVariavel getTipoVariavel() {
        return getNoSintatico().getTipo().getTipo();
    }
}
