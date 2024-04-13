package src.raiz.ast;

import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;

// Uma variável declarada explicitamente no código. Tem tipo, nome e tamanho.
public class Variavel extends NoSintatico {

    private TipoVariavelNo tipo;
    private final String nome;
    private final Integer tamanhoVetor; // se null, não é um vetor

    public Variavel(TipoVariavelNo tipo, Token token, Integer tamanhoVetor) throws ErroSemantico {
        super(token);
        this.tipo = tipo;
        this.nome = token.lexema();
        this.tamanhoVetor = tamanhoVetor;

        // Pode ser null na criação
        if (tipo != null && tipo.isTipoVazio()) {
            throw new ErroSemantico("Variável '" + nome + "' não pode ter tipo vazio", getToken());
        }
    }

    public TipoVariavelNo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVariavelNo tipo) throws ErroSemantico {
        if (tipo.isTipoVazio()) {
            throw new ErroSemantico("Variável '" + nome + "' não pode ter tipo vazio", getToken());
        }

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
