package src.raiz.ast;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.LinkedList;
import java.util.List;

// Declaração de variáveis no início de um bloco. São variáveis locais que só existem detro daquele bloco
public class DeclaracaoVariavelEmBloco extends Declaracao {

    private List<DeclaracaoDeVariavel> declaracoesDeVariaveis = new LinkedList<>();

    public DeclaracaoVariavelEmBloco(
            Token token,
            List<DeclaracaoDeVariavel> declaracoesDeVariaveis
    ) {
        super(token);
        this.declaracoesDeVariaveis = declaracoesDeVariaveis;
    }

    public DeclaracaoVariavelEmBloco() {
        super(null);
    }

    public List<DeclaracaoDeVariavel> getDeclaracoesDeVariaveis() {
        return declaracoesDeVariaveis;
    }

    @Override
    public String codigoOriginal() {
        return AstUtil.codigosOriginais(this.declaracoesDeVariaveis, "\n");
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "DeclaracaoVariavelEmBloco " + AstUtil.representacoesArvore(declaracoesDeVariaveis, profundidade - 1);
    }
}
