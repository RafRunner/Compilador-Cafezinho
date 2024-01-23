package src.raiz.compilador.tabeladesimbolos;

import src.raiz.erros.ErroSemantico;

import java.util.HashMap;
import java.util.Map;

// Classe para assciar Nomes a símbolos do programa. Variáveis globais e funções ficam no nível mais acima,
// E blocos abaxou tem acesso a elas e podem declarar novas variáveis locais com visibilidade somente
// naquele escopo criando uma nova TabelaDeSimbolos que tem a global como pai
public class TabelaDeSimbolos {

    private final Map<String, Simbolo<?>> variaveisEFuncoes = new HashMap<>();
    private final TabelaDeSimbolos tabelaPai;
    private final int offsetInicial;
    private int offset;

    private TabelaDeSimbolos(TabelaDeSimbolos pai, int offsetInicial) {
        this.tabelaPai = pai;
        this.offsetInicial = offsetInicial;
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

    public int getOffsetInicial() {
        return offsetInicial;
    }

    public void alteraOffset(int valor) {
        this.offset += valor;
    }

    public int getOffsetStack(int offsetVariavel) {
        return this.offset - offsetVariavel - 4;
    }

    public int getDiferencaOffset() {
        return this.offset - this.offsetInicial;
    }

    public void adicionaSimbolo(Simbolo<?> simbolo) {
        String nome = simbolo.getNome();
        Simbolo<?> simboloExistente = variaveisEFuncoes.get(nome);

        if (simboloExistente != null) {
            throw new ErroSemantico(
                "A " + simbolo.getTipoSimbolo().getDescricao() + " " + nome + " já foi declarada",
                simboloExistente.getNoSintatico().getToken()
            );
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
