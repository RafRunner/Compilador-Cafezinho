package src.raiz.ast.comandos;

import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.token.Token;

public class ComandoLeia extends Comando {

    private final ExpressaoIdentificador expressaoIdentificador;

    public ComandoLeia(Token token, ExpressaoIdentificador expressaoIdentificador) {
        super(token);
        this.expressaoIdentificador = expressaoIdentificador;
    }

    public ExpressaoIdentificador getExpressaoIdentificador() {
        return expressaoIdentificador;
    }

    @Override
    public String codigoOriginal() {
        return "leia " + expressaoIdentificador.codigoOriginal() + ";";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoLeia {\n"
               + getIndentacao(profundidade) + expressaoIdentificador.representacaoArvore(profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
