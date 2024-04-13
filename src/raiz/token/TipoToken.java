package src.raiz.token;

// Cada token tem um tipo. São os terminais da gramática, coisas que podem ser escritas.
public enum TipoToken {
    // Palavras reservadas
    PROGRAMA, CAR, INT, FLUT, VAZIO, RETORNE, LEIA, ESCREVA, NOVALINHA, SE, ENTAO, SENAO, ENQUANTO, EXECUTE,
    // Operadores lógicos
    OU, E, IGUAL, DIFERENTE, MENOR, MAIOR, MENOR_IGUAL, MAIOR_IGUAL, NEGACAO, TERNARIO,
    // Operadores matemáticos
    MAIS, MENOS, VEZES, DIVISAO, RESTO,
    // Outros
    ATRIBUICAO, VIRGULA, PONTO_E_VIRGULA, DOIS_PONTOS, ABRE_CHAVE, FECHA_CHAVE, ABRE_PARENTESES,
    FECHA_PARENTESES, ABRE_COLCHETE, FECHA_COLCHETE,
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
    EOF,
    // Funções nativas. Devem vir no fim
    FUNCAO_NATIVA,
}
