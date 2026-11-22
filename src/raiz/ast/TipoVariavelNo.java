package src.raiz.ast;

import src.raiz.token.Token;

// Nó que define o tipo de uma ou várias variáveis.
// Feito para ser um nó com valor de TipoVariavel
public class TipoVariavelNo extends NoSintatico {

    private final TipoVariavel tipo;

    public TipoVariavelNo(Token token, TipoVariavel tipo) {
        super(token);
        this.tipo = tipo;
    }

    public TipoVariavel getTipo() {
        return tipo;
    }

    @Override
    public String codigoOriginal() {
        return tipo.getLexema();
    }

    @Override
    public String toString() {
        return tipo.name();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return toString();
    }

}
