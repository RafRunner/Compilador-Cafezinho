package src.raiz.token;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import src.raiz.generated.TabelasGramatica;

// Descreve os tokens que o parser esperava num erro de sintaxe. Conjuntos que aparecem juntos com frequência viram
// uma palavra só, senão a mensagem lista dezenas de tokens (um comando pode começar de mais de 20 jeitos)
public class TokensEsperados {

    private record Grupo(String nome, Set<TipoToken> tokens) {
    }

    // Os grupos se sobrepõem: um comando pode começar com uma expressão, e o '-' é operador e também começa uma
    // expressão. Cada grupo completo só entra se ainda tiver algum token que nenhum grupo anterior descreveu
    // Comando, expressão e tipo são os conjuntos FIRST desses não terminais, gerados a partir da gramática. Os
    // operadores são o que pode vir depois de uma expressão completa, que não é um conjunto que a gramática defina
    // diretamente; se um operador novo faltar aqui, ele só aparece avulso na mensagem
    private static final List<Grupo> GRUPOS = List.of(
            new Grupo(
                    "operador", EnumSet.of(
                            TipoToken.COMO, TipoToken.OU, TipoToken.E, TipoToken.IGUAL, TipoToken.DIFERENTE,
                            TipoToken.MENOR, TipoToken.MAIOR, TipoToken.MENOR_IGUAL, TipoToken.MAIOR_IGUAL,
                            TipoToken.TERNARIO, TipoToken.MAIS, TipoToken.MENOS, TipoToken.VEZES, TipoToken.DIVISAO,
                            TipoToken.RESTO
                    )
            ),
            new Grupo("comando", TabelasGramatica.First.Comando),
            new Grupo("expressão", TabelasGramatica.First.Expr),
            new Grupo("tipo", TabelasGramatica.First.Tipo)
    );

    public static String descrever(Set<TipoToken> esperados) {
        Set<TipoToken> restantes = EnumSet.noneOf(TipoToken.class);
        restantes.addAll(esperados);

        List<String> descricoes = new ArrayList<>();
        for (Grupo grupo : GRUPOS) {
            if (esperados.containsAll(grupo.tokens()) && grupo.tokens().stream().anyMatch(restantes::contains)) {
                descricoes.add(grupo.nome());
                restantes.removeAll(grupo.tokens());
            }
        }
        for (TipoToken tipo : restantes) {
            descricoes.add(tipo.getDescricao());
        }

        if (descricoes.isEmpty()) {
            return "nada";
        }
        if (descricoes.size() == 1) {
            return descricoes.getFirst();
        }
        return String.join(", ", descricoes.subList(0, descricoes.size() - 1)) + " ou " + descricoes.getLast();
    }
}
