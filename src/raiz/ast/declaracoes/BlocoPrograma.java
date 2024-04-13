package src.raiz.ast.declaracoes;

import src.raiz.token.Token;

// Classe que agrupa o código na parte principal do programa (o bloco programa { ... })
// Ou seja, código que não está em uma função separada.
public class BlocoPrograma extends Declaracao {

    private final BlocoDeclaracoes blocoDeclaracoes;

    public BlocoPrograma(Token token, BlocoDeclaracoes blocoDeclaracoes) {
        super(token);
        this.blocoDeclaracoes = blocoDeclaracoes;
    }

    public BlocoDeclaracoes getBlocoDeclaracoes() {
        return blocoDeclaracoes;
    }

    @Override
    public String codigoOriginal() {
        return "programa " + blocoDeclaracoes.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return  "BlocoPrograma {\n"
                + getIndentacao(profundidade) + blocoDeclaracoes.representacaoArvore(profundidade + 1) + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}
