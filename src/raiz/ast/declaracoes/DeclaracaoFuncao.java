package src.raiz.ast.declaracoes;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.TipoVariavelNo;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.Collections;
import java.util.List;

// Declaração de uma função. Contem tudo necessário para chamá-la e executá-la
public class DeclaracaoFuncao extends Declaracao {

    private final TipoVariavelNo tipoRetorno;
    private final String nome;
    private final List<ParametroFuncao> parametros;
    private final BlocoDeclaracoes corpo;

    public DeclaracaoFuncao(
            Token token,
            TipoVariavelNo tipoRetorno,
            BlocoDeclaracoes corpo,
            List<ParametroFuncao> parametros
    ) {
        super(token);
        this.tipoRetorno = tipoRetorno;
        this.nome = token.lexema();
        this.parametros = Collections.unmodifiableList(parametros);
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
    public String codigoOriginal() {
        String paramString = AstUtil.codigosOriginais(this.parametros, ", ");

        return tipoRetorno.codigoOriginal() + " " + getToken().lexema() + "(" + paramString + ") "
                + corpo.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "DeclaracaoFuncao {\n"
                + getIndentacao(profundidade) + "nome: '" + nome + "',\n"
                + getIndentacao(profundidade) + "tipoRetorno: " + tipoRetorno + ",\n"
                + getIndentacao(profundidade) + "parametros: [" + AstUtil.toStrings(parametros, ", ") + "],\n"
                + getIndentacao(profundidade) + "corpo: " + corpo.representacaoArvore(profundidade + 1) + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}
