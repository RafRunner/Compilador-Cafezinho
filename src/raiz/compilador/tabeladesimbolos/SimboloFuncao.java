package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.DeclaracaoFuncao;
import src.raiz.ast.TipoVariavel;

public class SimboloFuncao extends Simbolo<DeclaracaoFuncao> {

    private final TabelaDeSimbolos tabelaDeSimbolos; // variáveis disponíveis na função

    public SimboloFuncao(DeclaracaoFuncao noSintatico, TabelaDeSimbolos tabelaDeSimbolos) {
        super(noSintatico, TipoSimbolo.FUNCAO);
        this.tabelaDeSimbolos = tabelaDeSimbolos;
    }

    public TabelaDeSimbolos getTabelaDeSimbolos() {
        return tabelaDeSimbolos;
    }

    @Override
    public TipoVariavel getTipoVariavel() {
        return getNoSintatico().getTipoRetorno().getTipo();
    }
}
