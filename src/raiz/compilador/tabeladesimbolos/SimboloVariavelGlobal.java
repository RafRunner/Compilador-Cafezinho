package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.Variavel;

public class SimboloVariavelGlobal extends Simbolo<Variavel> {

    // É o nome real da variável no programa. Isso é feito pois no SPIM alguns nomes não são aceitos
    private final String alias;

    public SimboloVariavelGlobal(Variavel noSintatico, String alias) {
        super(noSintatico, TipoSimbolo.VARIAVEL_GLOBAL);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
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
