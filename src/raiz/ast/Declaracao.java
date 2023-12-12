package src.raiz.ast;

import src.raiz.token.Token;

// Declaração é um pedaço de código que não produz um valor.
// Coisas como atribuição de valor a uma variável, um laço de repetição,
// uma declaração de uma função, etc
public abstract class Declaracao extends NoSintatico {

    public Declaracao(Token token) {
        super(token);
    }
}
