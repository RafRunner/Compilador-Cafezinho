package src.raiz.semantica;

import src.raiz.erros.ErroSemantico;

import java.util.HashMap;
import java.util.Map;

// Associa nomes a símbolos do programa. Variáveis globais e funções ficam no nível mais acima,
// e cada bloco cria uma tabela filha, onde pode declarar variáveis visíveis somente naquele escopo
public class TabelaDeSimbolos {

    private final Map<String, Simbolo> simbolos = new HashMap<>();
    private final TabelaDeSimbolos tabelaPai;

    private TabelaDeSimbolos(TabelaDeSimbolos pai) {
        this.tabelaPai = pai;
    }

    public TabelaDeSimbolos() {
        this(null);

        // Somente na tabela raiz adicionamos as funções nativas
        for (FuncaoNativa funcaoNativa : FuncaoNativa.values()) {
            adicionaSimbolo(new SimboloFuncaoNativa(funcaoNativa));
        }
    }

    public TabelaDeSimbolos criaBlocoInterno() {
        return new TabelaDeSimbolos(this);
    }

    // Cafezinho permite shadow de variáveis, mas não de funções. As verificações vão da mais específica para a mais
    // geral, para a mensagem dizer o motivo exato
    public void adicionaSimbolo(Simbolo simbolo) {
        String nome = simbolo.getNome();
        Simbolo visivel = getSimbolo(nome);

        // As nativas são reservadas em qualquer escopo, inclusive no global, onde também estão
        if (visivel instanceof SimboloFuncaoNativa) {
            throw new ErroSemantico(
                    "'" + nome + "' é uma função nativa e seu nome não pode ser reutilizado", simbolo.getToken()
            );
        }

        if (simbolos.containsKey(nome)) {
            throw new ErroSemantico(
                    "A " + simbolo.getDescricao() + " '" + nome + "' já foi declarada", simbolo.getToken()
            );
        }

        // Uma função de um escopo de fora: as funções só são declaradas no global, então isso é uma variável local
        // ou um parâmetro tentando esconder uma função
        if (visivel instanceof SimboloInvocavel) {
            throw new ErroSemantico(
                    "'" + nome + "' é uma função e seu nome não pode ser reutilizado", simbolo.getToken()
            );
        }

        simbolos.put(nome, simbolo);
    }

    public Simbolo getSimbolo(String nome) {
        Simbolo simbolo = simbolos.get(nome);

        if (simbolo == null && tabelaPai != null) {
            simbolo = tabelaPai.getSimbolo(nome);
        }

        return simbolo;
    }
}
