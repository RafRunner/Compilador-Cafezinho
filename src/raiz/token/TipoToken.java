package src.raiz.token;

// Cada token tem um tipo. São os terminais da gramática, coisas que podem ser escritas.
// O lexema só existe nos tokens que são sempre escritos do mesmo jeito; nos outros (identificadores, literais) ele
// vem do código fonte
public enum TipoToken {
    // Palavras reservadas
    PROGRAMA("programa"),
    CAR("car"),
    INT("int"),
    FLUT("flut"),
    VAZIO("vazio"),
    RETORNE("retorne"),
    LEIA("leia"),
    ESCREVA("escreva"),
    NOVALINHA("novalinha"),
    SE("se"),
    ENTAO("entao"),
    SENAO("senao"),
    ENQUANTO("enquanto"),
    EXECUTE("execute"),
    PARA("para"),
    CONTINUE("continue"),
    PARE("pare"),
    COMO("como"),
    // Operadores lógicos
    OU("ou"),
    E("e"),
    IGUAL("=="),
    DIFERENTE("!="),
    MENOR("<"),
    MAIOR(">"),
    MENOR_IGUAL("<="),
    MAIOR_IGUAL(">="),
    NEGACAO("!"),
    TERNARIO("?"),
    // Operadores matemáticos
    MAIS("+"),
    MENOS("-"),
    VEZES("*"),
    DIVISAO("/"),
    RESTO("%"),
    // Outros
    ATRIBUICAO("="),
    VIRGULA(","),
    PONTO_E_VIRGULA(";"),
    DOIS_PONTOS(":"),
    ABRE_CHAVE("{"),
    FECHA_CHAVE("}"),
    ABRE_PARENTESES("("),
    FECHA_PARENTESES(")"),
    ABRE_COLCHETE("["),
    FECHA_COLCHETE("]"),
    // Strings
    STRING_LITERAL,
    // Caracteres
    CARACTERE_LITERAL,
    // Identificadores
    IDENTIFICADOR,
    // Números
    INT_LITERAL,
    FLUT_LITERAL,
    // Fim do arquivo
    EOF;

    private final String lexema;

    TipoToken(String lexema) {
        this.lexema = lexema;
    }

    TipoToken() {
        this(null);
    }

    // Como o token aparece em mensagens, na mesma notação do docs/regras.txt: lexema entre aspas, ou o nome da
    // classe de token
    public String getDescricao() {
        if (this == EOF) {
            return "fim do arquivo";
        }
        return lexema != null ? "'" + lexema + "'" : name();
    }
}
