package src.raiz.erros;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

public class ErroSemantico extends RuntimeException {

    public ErroSemantico(String message, Token token) {
        super(AstUtil.montaMensagemErro("Erro semântico: " + message, token));
    }
}
