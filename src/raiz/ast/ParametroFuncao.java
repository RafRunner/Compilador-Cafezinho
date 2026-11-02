package src.raiz.ast;

import src.raiz.erros.BugCompilador;
import src.raiz.semantica.SimboloVariavel;
import src.raiz.token.Token;

// Nó que representa um parâmetro de uma declaração de função.
// Os parâmetros serão transformados em variáveis dentro do escopo da função
public class ParametroFuncao extends NoSintatico {

    private final TipoVariavelNo tipo;
    private final String nome;
    private final boolean vetor;

    // Preenchido pelo analisador Semântico
    private SimboloVariavel simbolo;

    public ParametroFuncao(Token token, TipoVariavelNo tipo, boolean vetor) {
        super(token);
        this.tipo = tipo;
        this.nome = token.lexema();
        this.vetor = vetor;
    }

    public TipoVariavelNo getTipo() {
        return tipo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isVetor() {
        return vetor;
    }

    public void setSimbolo(SimboloVariavel simbolo) {
        this.simbolo = simbolo;
    }

    public SimboloVariavel getSimbolo() {
        if (simbolo == null) {
            throw new BugCompilador("Parâmetro '" + nome + "' não resolvido pela análise semântica");
        }
        return simbolo;
    }

    @Override
    public String codigoOriginal() {
        return tipo.codigoOriginal() + " " + nome + (vetor ? "[]" : "");
    }

    @Override
    public String toString() {
        return "ParametroFuncao { " +
                "tipo: " + tipo +
                ", nome: '" + nome + '\'' +
                ", vetor: " + vetor +
                " }";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return toString();
    }
}
