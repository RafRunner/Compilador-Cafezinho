package src.raiz.ast;

// Toda variável tem um tipo
public enum TipoVariavel {
    INTEIRO("int"),
    CARACTERE("car");

    private final String lexema;

    TipoVariavel(String lexema) {
        this.lexema = lexema;
    }

    public String getLexema() {
        return lexema;
    }
}
