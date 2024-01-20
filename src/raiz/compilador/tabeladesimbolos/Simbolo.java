package src.raiz.compilador.tabeladesimbolos;

import src.raiz.ast.NoSintatico;
import src.raiz.ast.TipoVariavel;

// Classe abstrata que representa uma entrada em na tabela de Símbolos
public abstract class Simbolo<T extends NoSintatico> {

    private final TipoSimbolo tipoSimbolo;
    private final T noSintatico;

    public Simbolo(T noSintatico, TipoSimbolo tipoSimbolo) {
        this.tipoSimbolo = tipoSimbolo;
        this.noSintatico = noSintatico;
    }

    public T getNoSintatico() {
        return noSintatico;
    }

    public TipoSimbolo getTipoSimbolo() {
        return tipoSimbolo;
    }

    public abstract TipoVariavel getTipoVariavel();

    public String getNome() {
        return noSintatico.getToken().getLexema();
    }
}
