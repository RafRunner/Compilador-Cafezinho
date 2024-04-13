package src.raiz.ast.artificiais;

import src.raiz.ast.expressoes.Expressao;

// Expressão artifical que não produz nenhum valor. É a expressão de um retorno vazio, por exemplo
public class ExpressaoVazio extends Expressao {

    public ExpressaoVazio() {
        super(null);
    }

    @Override
    public String codigoOriginal() {
        return "expressão interna";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ExpressaoVazio {}";
    }
}
