package src.raiz.semantica;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.TipoVariavel;
import src.raiz.token.Token;

public record SimboloParametro(ParametroFuncao parametro) implements SimboloVariavel {

    @Override
    public String getNome() {
        return parametro.getNome();
    }

    @Override
    public String getDescricao() {
        return "parâmetro de função";
    }

    @Override
    public Token getToken() {
        return parametro.getToken();
    }

    @Override
    public TipoVariavel getTipo() {
        return parametro.getTipo().getTipo();
    }

    @Override
    public boolean isVetor() {
        return parametro.isVetor();
    }

    @Override
    public ParametroFuncao getDeclaracao() {
        return parametro;
    }
}
