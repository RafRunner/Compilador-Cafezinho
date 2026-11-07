package src.raiz.erros;

import src.raiz.token.Token;

// Sequência de tokens que a gramática não aceita
public class ErroSintatico extends ErroCompilacao {

    public ErroSintatico(String mensagem, Token token) {
        super(mensagem, token);
    }
}
