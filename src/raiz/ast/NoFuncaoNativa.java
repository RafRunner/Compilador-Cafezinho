package src.raiz.ast;

import src.raiz.compilador.FuncoesNativas;
import src.raiz.token.TipoToken;
import src.raiz.token.Token;

public class NoFuncaoNativa extends NoSintatico {

    private final FuncoesNativas funcaoNativa;

    public NoFuncaoNativa(FuncoesNativas funcaoNativa) {
        // Criando um token fake, já que funções nativas já estão declaradas.
        super(Token.criaTokenFake(TipoToken.FUNCAO_NATIVA, funcaoNativa.nome));
        this.funcaoNativa = funcaoNativa;
    }

    public FuncoesNativas getFuncaoNativa() {
        return funcaoNativa;
    }

    @Override
    public String representacaoString() {
        return funcaoNativa.nome;
    }
}
