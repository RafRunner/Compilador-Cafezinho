package src.raiz.ast.expressoes;

public enum OperadorUnario {
    NEGATIVO("-", "negativo"),
    NEGACAO("!", "negação");

    private final String lexema;
    private final String descricao;

    OperadorUnario(String lexema, String descricao) {
        this.lexema = lexema;
        this.descricao = descricao;
    }

    public String getLexema() {
        return lexema;
    }

    public String getDescricao() {
        return descricao;
    }
}
