package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.NoFuncaoNativa;
import src.raiz.ast.TipoVariavel;

public class SimboloFuncaoNativa extends Simbolo<NoFuncaoNativa> {

    public SimboloFuncaoNativa(NoFuncaoNativa noSintatico) {
        super(noSintatico, TipoSimbolo.FUNCAO_NATIVA);
    }

    @Override
    public TipoVariavel getTipoVariavel() {
        return getNoSintatico().getFuncaoNativa().tipoRetorno;
    }

    @Override
    public boolean isVetor() {
        return false;
    }
}
