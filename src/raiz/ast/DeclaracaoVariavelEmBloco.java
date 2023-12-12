package src.raiz.ast;

import src.raiz.token.Token;

import java.util.LinkedList;
import java.util.List;

// Declaração de variáveis no início de um bloco. São variáveis locais que só existem detro daquele bloco
public class DeclaracaoVariavelEmBloco extends Declaracao {

    private List<DeclaracaoDeVariavel> declaracoesDeVariaveis = new LinkedList<>();

    public DeclaracaoVariavelEmBloco(
            Token token,
            List<DeclaracaoDeVariavel> declaracoesDeVariaveis) {
        super(token);
        this.declaracoesDeVariaveis = declaracoesDeVariaveis;
    }

    public DeclaracaoVariavelEmBloco() {
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
        if (declaracoesDeVariaveis.isEmpty()) {
            return "DeclaracaoVariavelEmBloco {}";
        }
        return "DeclaracaoVariavelEmBloco {" +
                "\ndeclaracoesDeVariaveis=" + declaracoesDeVariaveis +
                "\n}\n";
    }
}
