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
    public String codigoOriginal() {
        return funcaoNativa.nome;
    }

    @Override
    public String toString() {
        return "NoFuncaoNativa { nome: " + funcaoNativa.nome + " }";
    }

    // Esse nó não pode ser encontrado normalmente
    @Override
    public String representacaoArvore(int profundidade) {
        return "expressão interna";
    }
}
