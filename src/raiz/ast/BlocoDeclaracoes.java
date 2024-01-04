package src.raiz.ast;

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
    public String representacaoString() {
        StringBuilder sb = new StringBuilder("{\n");
        AstUtil.representacoesString(sb, this.declaracoes, "\n");
        sb.append("\n}");

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder declaracoesStr = new StringBuilder();
        AstUtil.toStrings(declaracoesStr, this.declaracoes, ",\n");

        return "BlocoDeclaracoes {\n" + declaracoesStr + "\n}";
    }
}
