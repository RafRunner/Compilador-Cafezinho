package src.raiz.ast.expressoes;

import src.raiz.erros.BugCompilador;
import src.raiz.semantica.SimboloVariavel;
import src.raiz.token.Token;

public final class ExpressaoIdentificador extends Expressao {

    private final String identificador;
    private final Expressao index;

    // Variável a que o identificador se refere, preenchida pelo AnalisadorSemantico
    private SimboloVariavel simbolo;

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

    public SimboloVariavel getSimbolo() {
        if (simbolo == null) {
            throw new BugCompilador("Identificador '" + identificador + "' não resolvido pela análise semântica");
        }
        return simbolo;
    }

    public void setSimbolo(SimboloVariavel simbolo) {
        this.simbolo = simbolo;
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

        return "ExpressaoIdentificador {\n"
               + getIndentacao(profundidade) + identificador + "[" + index.representacaoArvore(profundidade + 1) + "]\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
