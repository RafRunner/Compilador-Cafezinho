package src.raiz.ast.expressoes;

// Operadores binários da linguagem. A categoria agrupa operadores que seguem as mesmas regras de tipos
public enum OperadorBinario {
    SOMA("+", "soma", Categoria.ARITMETICA),
    SUBTRACAO("-", "subtração", Categoria.ARITMETICA),
    MULTIPLICACAO("*", "multiplicação", Categoria.ARITMETICA),
    DIVISAO("/", "divisão", Categoria.ARITMETICA),
    RESTO("%", "resto", Categoria.INTEIRA),
    IGUAL("==", "igual", Categoria.COMPARACAO),
    DIFERENTE("!=", "diferente", Categoria.COMPARACAO),
    MENOR("<", "menor", Categoria.COMPARACAO),
    MAIOR(">", "maior", Categoria.COMPARACAO),
    MENOR_IGUAL("<=", "menor igual", Categoria.COMPARACAO),
    MAIOR_IGUAL(">=", "maior igual", Categoria.COMPARACAO),
    E("e", "e", Categoria.LOGICA),
    OU("ou", "ou", Categoria.LOGICA);

    public enum Categoria {
        ARITMETICA, // entre números (int ou flut), resultado do tipo mais amplo
        INTEIRA,    // somente entre inteiros
        COMPARACAO, // entre valores do mesmo tipo, resultado inteiro (booleano)
        LOGICA      // entre inteiros (booleanos), com curto-circuito
    }

    private final String lexema;
    private final String descricao;
    private final Categoria categoria;

    OperadorBinario(String lexema, String descricao, Categoria categoria) {
        this.lexema = lexema;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    public String getLexema() {
        return lexema;
    }

    public String getDescricao() {
        return descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
