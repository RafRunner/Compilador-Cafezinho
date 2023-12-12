package src.raiz.ast;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.List;

// Declaração de uma função. Contem tudo necessário para chamá-la e executá-la
public class DeclaracaoFuncao extends Declaracao {

    private final TipoVariavelNo tipoRetorno;
    private final String nome;
    private final List<ParametroFuncao> parametros;
    private final BlocoDeclaracoes corpo;

    public DeclaracaoFuncao(Token token, TipoVariavelNo tipoRetorno, BlocoDeclaracoes corpo,
            List<ParametroFuncao> parametros) {
        super(token);
        this.tipoRetorno = tipoRetorno;
        this.nome = token.getLexema();
        this.parametros = parametros;
        this.corpo = corpo;
    }

    public TipoVariavelNo getTipoRetorno() {
        return tipoRetorno;
    }

    public String getNome() {
        return nome;
    }

    public List<ParametroFuncao> getParametros() {
        return parametros;
    }

    public BlocoDeclaracoes getCorpo() {
        return corpo;
    }

    @Override
    public String representacaoString() {
        StringBuilder paramString = new StringBuilder();
        AstUtil.representacoesString(paramString, this.parametros, ", ");

        return tipoRetorno.representacaoString() + " " + getToken().getLexema() + " (" + paramString + ")"
                + corpo.representacaoString();
    }

    @Override
    public String toString() {
        return "DeclaracaoFuncao {\n" +
                "tipoRetorno=" + tipoRetorno +
                ",\nparametros=" + parametros +
                ",\ncorpo=" + corpo +
                "\n}\n";
    }
}
