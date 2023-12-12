package src.raiz.ast;

import src.raiz.token.Token;

// Uma variável declarada explicitamente no código. Tem tipo, nome e tamanho.
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
        String base = "Variavel[tipo=" + tipo + ", nome=" + nome;
        if (tamanhoVetor == null) {
            return base + "]";
        } else {
            return base + ", tamanhoVetor=" + tamanhoVetor + "]";
        }
    }

    @Override
    public String representacaoString() {
        return tipo.representacaoString() + " " + declaracaoSemTipo();
    }

}
