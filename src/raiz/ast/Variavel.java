package src.raiz.ast;

import src.raiz.token.Token;

public class Variavel extends NoSintatico {

    private TipoVariavelNo tipo;
    private final String nome;
    private final Integer tamanhoVetor; // se null, não é um vetor

    public Variavel(TipoVariavelNo tipo, Token token, Integer tamanhoVetor) {
        super(token);
        this.tipo = tipo;
        this.nome = token.getLexema();
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

    public String declaracaoSemTipo() {
        return nome + (tamanhoVetor != null ? ("[" + tamanhoVetor + "]") : "");
    }

    @Override
    public String toString() {
        return "Variavel [tipo=" + tipo + ", nome=" + nome + ", tamanhoVetor=" + tamanhoVetor + "]";
    }

    @Override
    public String representacaoString() {
        return tipo.representacaoString() + " " + declaracaoSemTipo();
    }

}
