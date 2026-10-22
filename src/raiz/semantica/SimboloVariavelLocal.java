package src.raiz.semantica;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.Variavel;
import src.raiz.token.Token;

public record SimboloVariavelLocal(Variavel variavel) implements SimboloVariavel {

    @Override
    public String getNome() {
        return variavel.getNome();
    }

    @Override
    public String getDescricao() {
        return "variável local";
    }

    @Override
    public Token getToken() {
        return variavel.getToken();
    }

    @Override
    public TipoVariavel getTipo() {
        return variavel.getTipo().getTipo();
    }

    @Override
    public boolean isVetor() {
        return variavel.isVetor();
    }

    @Override
    public Variavel getDeclaracao() {
        return variavel;
    }
}
