package src.raiz.semantica;

import src.raiz.compilador.FuncoesNativas;
import src.raiz.token.Token;

public record SimboloFuncaoNativa(FuncoesNativas funcao) implements SimboloInvocavel {

    @Override
    public String getNome() {
        return funcao.nome;
    }

    @Override
    public String getDescricao() {
        return "função nativa";
    }

    @Override
    public Token getToken() {
        return null;
    }
}
