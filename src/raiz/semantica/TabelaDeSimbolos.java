package src.raiz.semantica;

import src.raiz.compilador.FuncoesNativas;
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

        for (FuncoesNativas funcaoNativa : FuncoesNativas.values()) {
            adicionaSimbolo(new SimboloFuncaoNativa(funcaoNativa));
        }
    }

    public TabelaDeSimbolos() {
        this(null);
    }

    public TabelaDeSimbolos criaBlocoInterno() {
        return new TabelaDeSimbolos(this);
    }

    public void adicionaSimbolo(Simbolo simbolo) {
        String nome = simbolo.getNome();
        Simbolo simboloExistente = simbolos.get(nome);

        if (simboloExistente != null) {
            if (simboloExistente instanceof SimboloFuncaoNativa) {
                throw new ErroSemantico(
                        "'" + nome + "' é uma função nativa e seu nome não pode ser reutilizado",
                        simbolo.getToken()
                );
            }

            throw new ErroSemantico(
                    "A " + simbolo.getDescricao() + " '" + nome + "' já foi declarada",
                    simbolo.getToken()
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
