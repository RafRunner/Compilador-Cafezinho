package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.Variavel;

public class SimboloVariavelGlobal extends Simbolo<Variavel> {

    public SimboloVariavelGlobal(Variavel noSintatico) {
        super(noSintatico, TipoSimbolo.VARIAVEL_GLOBAL);
    }
}
