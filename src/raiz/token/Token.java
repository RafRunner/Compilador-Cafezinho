package src.raiz.token;

// Uma "palavra" no código. É associado a algo que foi digitado pelo programador,
// logo tem lexema e localização
public record Token(TipoToken tipo, String lexema, int linha, int coluna) implements Comparable<Token> {

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

    // Ordena por ordem que aparece no código fonte
    @Override
    public int compareTo(Token o) {
        if (linha() == o.linha()) {
            return coluna() - o.coluna();
        }

        return linha() - o.linha();
    }
}
