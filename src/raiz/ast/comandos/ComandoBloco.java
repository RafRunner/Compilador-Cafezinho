package src.raiz.ast.comandos;

import src.raiz.ast.BlocoDeclaracoes;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

public class ComandoBloco extends Comando {

    private final BlocoDeclaracoes declaracoes;

    public ComandoBloco(Token token, BlocoDeclaracoes declaracoes) {
        super(token);
        this.declaracoes = declaracoes;
    }

    public BlocoDeclaracoes getDeclaracoes() {
        return declaracoes;
    }

    @Override
    public String codigoOriginal() {
        return declaracoes.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ComandoBloco {\n"
               + getIdentacao(profundidade) + declaracoes.representacaoArvore(profundidade + 1) + "\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}
