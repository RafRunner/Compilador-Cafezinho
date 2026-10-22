package src.raiz.semantica;

import src.raiz.token.Token;

public record SimboloFuncaoNativa(FuncaoNativa funcao) implements SimboloInvocavel {

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
