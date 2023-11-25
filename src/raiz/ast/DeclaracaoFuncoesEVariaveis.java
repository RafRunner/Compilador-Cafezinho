package src.raiz.ast;

import src.raiz.token.Token;

import java.util.LinkedList;
import java.util.List;

public class DeclaracaoFuncoesEVariaveis extends Declaracao {

    private final List<DeclaracaoDeVariavel> declaracoesDeVariaveis = new LinkedList<>();

    public DeclaracaoFuncoesEVariaveis(
            Token token,
            List<? extends Declaracao> declaracoes) {
        super(token);
        for (Declaracao declaracao : declaracoes) {
            if (declaracao instanceof DeclaracaoDeVariavel) {
                declaracoesDeVariaveis.add((DeclaracaoDeVariavel) declaracao);
            } else {
                throw new RuntimeException("Declaração de tipo inexperado! " + declaracao);
            }
        }
    }

    public DeclaracaoFuncoesEVariaveis() {
        super(null);
    }

    public List<DeclaracaoDeVariavel> getDeclaracoesDeVariaveis() {
        return declaracoesDeVariaveis;
    }

    @Override
    public String representacaoString() {
        StringBuilder sb = new StringBuilder();

        for (Declaracao declaracao : this.declaracoesDeVariaveis) {
            sb.append(declaracao.representacaoString()).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "DeclaracaoFuncoesEVariaveis\n{" +
                "\ndeclaracoesDeVariaveis=" + declaracoesDeVariaveis +
                "\n}\n";
    }
}
