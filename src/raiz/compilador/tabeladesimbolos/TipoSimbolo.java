package src.raiz.compilador.tabeladesimbolos;

public enum TipoSimbolo {

    VARIAVEL_GLOBAL("variável global"),
    VARIAVEL_LOCAL("variável local"),
    PARAMETRO_FUNCAO("parâmetro de função"),
    FUNCAO("função");

    private final String descricao;

    TipoSimbolo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
