package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.DeclaracaoFuncao;

public class SimboloFuncao extends Simbolo<DeclaracaoFuncao> {

    private final TabelaDeSimbolos tabelaDeSimbolos; // variáveis disponíveis na função

    public SimboloFuncao(DeclaracaoFuncao noSintatico, TabelaDeSimbolos tabelaDeSimbolos) {
        super(noSintatico, TipoSimbolo.FUNCAO);
        this.tabelaDeSimbolos = tabelaDeSimbolos;
    }

    public TabelaDeSimbolos getTabelaDeSimbolos() {
        return tabelaDeSimbolos;
    }
}
