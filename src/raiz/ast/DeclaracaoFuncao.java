package src.raiz.ast;

import src.raiz.token.Token;

import java.util.List;

public class DeclaracaoFuncao extends Declaracao {

    private final TipoVariavelNo tipoRetorno;
    private final BlocoDeclaracoes corpo;
    private final List<ParametroFuncao> parametros;

    public DeclaracaoFuncao(Token token, TipoVariavelNo tipoRetorno, BlocoDeclaracoes corpo,
            List<ParametroFuncao> parametros) {
        super(token);
        this.tipoRetorno = tipoRetorno;
        this.corpo = corpo;
        this.parametros = parametros;
    }

    public TipoVariavelNo getTipoRetorno() {
        return tipoRetorno;
    }

    public BlocoDeclaracoes getCorpo() {
        return corpo;
    }

    public List<ParametroFuncao> getParametros() {
        return parametros;
    }

    @Override
    public String representacaoString() {
        StringBuilder paramString = new StringBuilder();

        for (int i = 0; i < paramString.length(); i++) {
            paramString.append(parametros.get(i).representacaoString());
            if (i < parametros.size() - 1) {
                paramString.append(", ");
            }
        }

        return tipoRetorno.representacaoString() + " " + getToken().getLexema() + " (" + paramString + ")"
                + corpo.representacaoString();
    }

    @Override
    public String toString() {
        return "DeclaracaoFuncao\n{\n" +
                "tipoRetorno=" + tipoRetorno +
                ",\nparametros=" + parametros +
                ",\ncorpo=" + corpo +
                "\n}\n";
    }
}
