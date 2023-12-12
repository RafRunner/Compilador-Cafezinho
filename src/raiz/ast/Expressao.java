package src.raiz.ast;

import src.raiz.token.Token;

// Expressao é um pedaço de código que produz um valor.
// Coisas como operações lógicas (produzem verdadeiro ou falso),
// uma operação de Infixo (como a soma de dois números, que produz um número), etc
public abstract class Expressao extends NoSintatico {

    public Expressao(Token token) {
        super(token);
    }
}
