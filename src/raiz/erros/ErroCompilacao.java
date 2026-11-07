package src.raiz.erros;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

// Erro no programa cafezinho sendo compilado, com a posição no código fonte. É o que o usuário deve ver e corrigir,
// ao contrário do BugCompilador, que é um erro no próprio compilador
public abstract class ErroCompilacao extends RuntimeException {

    protected ErroCompilacao(String mensagem, int linha, int coluna, String proximoDe) {
        super(AstUtil.montaMensagemErro(mensagem, linha, coluna, proximoDe));
    }

    protected ErroCompilacao(String mensagem, Token token) {
        this(mensagem, token.linha(), token.coluna(), token.lexema());
    }
}
