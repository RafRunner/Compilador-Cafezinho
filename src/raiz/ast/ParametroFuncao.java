package src.raiz.ast;

import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;

// Nó que representa um parâmetro de uma declaração de função.
// Os parâmetros serão transformados em variáveis dentro do escopo da função
public class ParametroFuncao extends NoSintatico {

    private final TipoVariavelNo tipo;
    private final String nome;
    private final boolean vetor;

    public ParametroFuncao(Token token, TipoVariavelNo tipo, boolean vetor) throws ErroSemantico {
        super(token);
        this.tipo = tipo;
        this.nome = token.lexema();
        this.vetor = vetor;

        if (tipo.isTipoVazio()) {
            throw new ErroSemantico("Parâmetro '" + nome + "' não pode ter tipo vazio", token);
        }
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
