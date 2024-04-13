package src.raiz.ast.declaracoes;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.LinkedList;
import java.util.List;

// Basicamente um conjunto de linhas (comandos) em um escopo.
// Ex: uma função tem um corpo. Esse corpo é um Bloco de Declarações.
public class BlocoDeclaracoes extends Declaracao {

    private final List<Declaracao> declaracoes = new LinkedList<>();

    public BlocoDeclaracoes(Token token) {
        super(token);
    }

    public List<Declaracao> getDeclaracoes() {
        return declaracoes;
    }

    @Override
    public String codigoOriginal() {
        return "{\n" + AstUtil.codigosOriginais(this.declaracoes, "\n") + "\n}";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "BlocoDeclaracoes " + AstUtil.representacoesArvore(this.declaracoes, profundidade);
    }
}
