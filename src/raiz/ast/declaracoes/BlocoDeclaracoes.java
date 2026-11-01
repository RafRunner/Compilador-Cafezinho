package src.raiz.ast.declaracoes;

import src.raiz.ast.comandos.Comando;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.Collections;
import java.util.List;

// Um escopo: as declarações de variáveis locais, que sempre vêm no início, seguidas dos comandos.
// Ex: uma função tem um corpo. Esse corpo é um Bloco de Declarações.
public class BlocoDeclaracoes extends Declaracao {

    private final DeclaracaoVariavelEmBloco variaveis;
    private final List<Comando> comandos;

    public BlocoDeclaracoes(Token token, DeclaracaoVariavelEmBloco variaveis, List<Comando> comandos) {
        super(token);
        this.variaveis = variaveis;
        this.comandos = Collections.unmodifiableList(comandos);
    }

    public DeclaracaoVariavelEmBloco getVariaveis() {
        return variaveis;
    }

    public List<Comando> getComandos() {
        return comandos;
    }

    @Override
    public String codigoOriginal() {
        String codigoVariaveis;
        if (variaveis.getDeclaracoesDeVariaveis().isEmpty()) {
            codigoVariaveis = "";
        } else {
            codigoVariaveis = variaveis.codigoOriginal() + "\n";
        }
        return "{\n" + codigoVariaveis + AstUtil.codigosOriginais(comandos, "\n") + "\n}";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "BlocoDeclaracoes {\n" +
                AstUtil.getIndentacao(profundidade) + "variaveis: "
                + variaveis.representacaoArvore(profundidade + 1) + "\n"
                + AstUtil.getIndentacao(profundidade) + "comandos: "
                + AstUtil.representacoesArvore(comandos, profundidade + 1)
                + "\n}";
    }
}
