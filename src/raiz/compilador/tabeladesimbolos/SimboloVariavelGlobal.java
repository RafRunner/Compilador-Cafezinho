package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.Variavel;

public class SimboloVariavelGlobal extends Simbolo<Variavel> {

    // É o nome real da variável no programa. Isso é feito pois no SPIM variáveis globais só podem ter 4 caracteres...
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
}
