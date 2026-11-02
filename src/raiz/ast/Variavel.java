package src.raiz.ast;

import src.raiz.erros.BugCompilador;
import src.raiz.semantica.SimboloVariavel;
import src.raiz.token.Token;

// Uma variável declarada explicitamente no código. Tem tipo, nome e tamanho.
public class Variavel extends NoSintatico {

    private TipoVariavelNo tipo;
    private final String nome;
    private final Integer tamanhoVetor; // se null, não é um vetor

    // Preenchido pelo analisador Semântico
    private SimboloVariavel simbolo;

    // O tipo pode ser null na criação, sendo definido depois com setTipo
    public Variavel(TipoVariavelNo tipo, Token token, Integer tamanhoVetor) {
        super(token);
        this.tipo = tipo;
        this.nome = token.lexema();
        this.tamanhoVetor = tamanhoVetor;
    }

    public TipoVariavelNo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVariavelNo tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public Integer getTamanhoVetor() {
        return tamanhoVetor;
    }

    public boolean isVetor() {
        return tamanhoVetor != null;
    }

    public String declaracaoSemTipo() {
        return nome + (tamanhoVetor != null ? ("[" + tamanhoVetor + "]") : "");
    }

    public void setSimbolo(SimboloVariavel simbolo) {
        this.simbolo = simbolo;
    }

    public SimboloVariavel getSimbolo() {
        if (simbolo == null) {
            throw new BugCompilador("Variável '" + nome + "' não resolvido pela análise semântica");
        }
        return simbolo;
    }

    @Override
    public String toString() {
        String base = "Variavel { tipo: " + tipo + ", nome: " + nome;
        if (tamanhoVetor == null) {
            return base + " }";
        } else {
            return base + ", tamanhoVetor: " + tamanhoVetor + " }";
        }
    }

    @Override
    public String codigoOriginal() {
        return tipo.codigoOriginal() + " " + declaracaoSemTipo();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return this.toString();
    }
}
