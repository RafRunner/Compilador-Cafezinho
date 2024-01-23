package src.raiz.ast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Classe que representa a parte inicial do programa, onde declaramos variáveis globais
// e funções.
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

    // Retorna as declarações de funções e variáveis globais na ordem que aparecem no programa
    public List<Declaracao> getDeclaracoesEmOrdem() {
        List<Declaracao> todas = new ArrayList<>(this.declaracoesDeVariaveis);
        todas.addAll(this.declaracoesDeFuncoes);

        todas.sort((a, b) -> {
            if (a.getToken().getLinha() == b.getToken().getLinha()) {
                return a.getToken().getColuna() - b.getToken().getColuna();
            }
            return a.getToken().getLinha() - b.getToken().getLinha();
        });

        return todas;
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
        return "DeclaracaoFuncoesEVariaveis {" +
                "\ndeclaracoesDeVariaveis=" + declaracoesDeVariaveis +
                ",\ndeclaracoesDeFuncoes=" + declaracoesDeFuncoes +
                "\n}\n";
    }
}
