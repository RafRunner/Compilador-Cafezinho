package src.raiz.ast.comandos;

import java.util.EnumSet;
import java.util.Set;

import src.raiz.ast.TipoVariavel;
import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.token.Token;

public final class ComandoLeia extends Comando {

    private static final Set<TipoVariavel> TIPOS_SUPORTADOS = EnumSet.of(
            TipoVariavel.CARACTERE, TipoVariavel.INTEIRO, TipoVariavel.FLUTUANTE
    );

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

    public static boolean isTipoSuportado(TipoVariavel tipo) {
        return TIPOS_SUPORTADOS.contains(tipo);
    }
}
