package src.raiz.token;

public enum TipoToken {
    // Palavras reservadas
    PROGRAMA, CAR, INT, RETORNE, LEIA, ESCREVA, NOVALINHA, SE, ENTAO, SENAO, ENQUANTO, EXECUTE,
    // Operadores lógicos
    OU, E, IGUAL, DIFERENTE, MENOR, MAIOR, MENOR_IGUAL, MAIOR_IGUAL, NEGACAO, TERNARIO,
    // Operadores matemáticos
    MAIS, MENOS, VEZES, DIVISAO, RESTO,
    // Outros
    ATRIBUICAO, VIRGULA, PONTO_E_VIRGULA, DOIS_PONTOS, ABRE_CHAVE, FECHA_CHAVE, ABRE_PARENTESES,
    FECHA_PARENTESES, ABRE_COLCHETE, FECHA_COLCHETE,
    // Strings
    LITERAL_STRING,
    // Identificadores
    IDENTIFICADOR,
    // Números
    INT_LITERAL,
    // Fim do arquivo
    EOF
}
