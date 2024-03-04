package src.raiz.token;

// Uma "palavra" no código. É associado a algo que foi digitado pelo programador,
// logo tem lexema e localização
public class Token {
    private final TipoToken tipo;
    private final String lexema;
    private final int linha;
    private final int coluna;

    public Token(TipoToken tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public String descricaoLocal() {
        return "linha " + (linha + 1) + " coluna " + (coluna + 1);
    }

    @Override
    public String toString() {
        return "Token{tipo:" + tipo + ", lexema:'" + lexema + "', linha:" + linha + ", coluna:" + coluna + "}";
    }

    // Usado para funções nativas que não tem código fonte
    public static Token criaTokenFake(TipoToken tipo, String lexema) {
        return new Token(tipo, lexema, -1, -1);
    }
}
