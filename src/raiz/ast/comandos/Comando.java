package src.raiz.ast.comandos;

import src.raiz.ast.declaracoes.Declaracao;
import src.raiz.token.Token;

// Classe abstrata para classificar Comandos
public abstract sealed class Comando extends Declaracao
        permits ComandoBloco, ComandoComExpressao, ComandoEnquanto, ComandoEscreva, ComandoLeia, ComandoNovalinha,
                ComandoRetorno, ComandoSe, ComandoVazio {

    public Comando(Token token) {
        super(token);
    }
}
