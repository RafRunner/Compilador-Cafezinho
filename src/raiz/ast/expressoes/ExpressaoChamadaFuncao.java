package src.raiz.ast.expressoes;

import java.util.Collections;
import java.util.List;

import src.raiz.token.Token;
import src.raiz.util.AstUtil;

public class ExpressaoChamadaFuncao extends Expressao {

    private final String nomeFuncao;
    private final List<Expressao> argumentos;

    public ExpressaoChamadaFuncao(Token token, List<Expressao> argumentos) {
        super(token);
        this.nomeFuncao = token.lexema();
        this.argumentos = Collections.unmodifiableList(argumentos);
    }

    public String getNomeFuncao() {
        return nomeFuncao;
    }

    public List<Expressao> getArgumentos() {
        return argumentos;
    }

    @Override
    public String codigoOriginal() {
        return nomeFuncao + "(" + AstUtil.codigosOriginais(argumentos, ", ") + ")";
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "ExpressaoChamadaFuncao {\n"
               + getIndentacao(profundidade) + "nomeFuncao: '" + nomeFuncao + "',\n"
               + getIndentacao(profundidade) + "argumentos: " + AstUtil.representacoesArvore(argumentos, profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
