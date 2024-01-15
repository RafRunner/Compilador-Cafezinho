package src.raiz.compilador.tabeladesimbolos;

import java.util.HashMap;
import java.util.Map;

// Classe para assciar Nomes a símbolos do programa. Variáveis globais e funções ficam no nível mais acima,
// E blocos abaxou tem acesso a elas e podem declarar novas variáveis locais com visibilidade somente
// naquele escopo criando uma nova TabelaDeSimbolos que tem a global como pai
public class TabelaDeSimbolos {

    private final Map<String, Simbolo<?>> variaveisEFuncoes = new HashMap<>();
    private final TabelaDeSimbolos tabelaPai;
    private int offset;

    private TabelaDeSimbolos(TabelaDeSimbolos pai, int offsetInicial) {
        this.tabelaPai = pai;
        this.offset = offsetInicial;
    }

    public TabelaDeSimbolos() {
        this(null, 0);
    }

    public TabelaDeSimbolos criaBlocoInterno() {
        return new TabelaDeSimbolos(this, offset);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void adicionaSimbolo(Simbolo<?> simbolo) {
        String nome = simbolo.getNome();
        Simbolo<?> simboloExistente = variaveisEFuncoes.get(nome);

        if (simboloExistente != null) {
            throw new RuntimeException("A " + simbolo.getTipoSimbolo().getDescricao() + " já foi declarada em "
                    + simboloExistente.getNoSintatico().getToken().descricaoLocal());
        }

        variaveisEFuncoes.put(nome, simbolo);
    }

    public Simbolo<?> getSimbolo(String nome) {
        Simbolo<?> valorLocal = variaveisEFuncoes.get(nome);

        if (valorLocal == null && tabelaPai != null) {
            valorLocal = tabelaPai.getSimbolo(nome);
        }

        return valorLocal;
    }
}
