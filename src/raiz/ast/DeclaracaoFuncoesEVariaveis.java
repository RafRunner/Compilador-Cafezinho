package src.raiz.ast;

import java.util.LinkedList;
import java.util.List;

public class DeclaracaoFuncoesEVariaveis extends Declaracao {

    private final List<DeclaracaoDeVariavel> declaracoesDeVariaveis = new LinkedList<>();
    private final List<DeclaracaoFuncao> declaracoesDeFuncoes = new LinkedList<>();

    public DeclaracaoFuncoesEVariaveis() {
        super(null);
    }

    public List<DeclaracaoDeVariavel> getDeclaracoesDeVariaveis() {
        return declaracoesDeVariaveis;
    }

    public List<DeclaracaoFuncao> getDeclaracoesDeFuncoes() {
        return declaracoesDeFuncoes;
    }

    @Override
    public String representacaoString() {
        StringBuilder sb = new StringBuilder();

        for (Declaracao declaracao : this.declaracoesDeVariaveis) {
            sb.append(declaracao.representacaoString()).append("\n");
        }
        for (Declaracao declaracao : this.declaracoesDeFuncoes) {
            sb.append(declaracao.representacaoString()).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "DeclaracaoFuncoesEVariaveis\n{" +
                "\ndeclaracoesDeVariaveis=" + declaracoesDeVariaveis +
                ",\ndeclaracoesDeFuncoes=" + declaracoesDeFuncoes +
                "\n}\n";
    }
}
