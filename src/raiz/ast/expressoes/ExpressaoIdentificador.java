package src.raiz.ast.expressoes;

import src.raiz.token.Token;

public class ExpressaoIdentificador extends Expressao {

    private final String identificador;
    private final Expressao index;

    public ExpressaoIdentificador(Token token, Expressao index) {
        super(token);
        this.identificador = token.lexema();
        this.index = index;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Expressao getIndex() {
        return index;
    }

    @Override
    public String codigoOriginal() {
        return identificador + (index != null ? ("[" + index.codigoOriginal() + "]") : "");
    }

    @Override
    public String representacaoArvore(int profundidade) {
        if (index == null) {
            return "ExpressaoIdentificador { " + identificador + " }";
        }

        return "ExpressaoIdentificador { " +  identificador
               + "[ " + index.representacaoArvore(profundidade + 1) + " ] }";
    }
}
