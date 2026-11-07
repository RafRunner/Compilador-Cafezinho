package src.raiz.erros;

import src.raiz.token.Token;

// Programa sintaticamente correto que quebra uma regra da linguagem: tipos incompatíveis, nome não declarado...
public class ErroSemantico extends ErroCompilacao {

    public ErroSemantico(String mensagem, Token token) {
        super("Erro semântico: " + mensagem, token);
    }
}
