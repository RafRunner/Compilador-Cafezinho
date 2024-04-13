package src.raiz.ast;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

// Tudo que faz parte da Árvore Sintática é um nó sintático
public abstract class NoSintatico {

    // Tudo que é um nó sintátio tem um token. Isso serve para ligar a estrutura com
    // uma localização no código (linha e coluna), um lexema e um tipo.
    private Token token;

    public NoSintatico(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    protected void setToken(Token token) {
        this.token = token;
    }

    protected String getIndentacao(int profundidade) {
        return AstUtil.getIndentacao(profundidade);
    }

    // É a reconstrução aproximada do código original que levou a esse NoSintatico
    public abstract String codigoOriginal();

    // Imprime uma representação desse nodo considerando a profundidade
    public abstract String representacaoArvore(int profundidade);

    @Override
    public String toString() {
        return representacaoArvore(1);
    }
}
