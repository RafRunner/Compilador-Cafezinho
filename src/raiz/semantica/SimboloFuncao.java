package src.raiz.semantica;

import src.raiz.ast.declaracoes.DeclaracaoFuncao;
import src.raiz.token.Token;

public record SimboloFuncao(DeclaracaoFuncao declaracao) implements SimboloInvocavel {

    @Override
    public String getNome() {
        return declaracao.getNome();
    }

    @Override
    public String getDescricao() {
        return "função";
    }

    @Override
    public Token getToken() {
        return declaracao.getToken();
    }
}
