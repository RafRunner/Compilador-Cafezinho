package src.raiz.ast.comandos;

import src.raiz.ast.declaracoes.Declaracao;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

// Classe abstrata para classificar Comandos
public abstract sealed class Comando extends Declaracao
        permits ComandoBloco, ComandoComExpressao, ComandoEnquanto, ComandoEscreva, ComandoLeia, ComandoNovalinha,
        ComandoRetorno, ComandoSe, ComandoVazio {

    public Comando(Token token) {
        super(token);
    }

    // Código do comando que é o corpo de um se ou enquanto: um bloco abre na mesma linha,
    // um comando simples vai para a linha de baixo, indentado
    protected static String codigoCorpo(Comando corpo) {
        if (corpo instanceof ComandoBloco) {
            return " " + corpo.codigoOriginal();
        }
        return "\n" + AstUtil.indenta(corpo.codigoOriginal());
    }
}
