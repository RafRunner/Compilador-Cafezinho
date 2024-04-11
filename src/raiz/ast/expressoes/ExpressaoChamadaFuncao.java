package src.raiz.ast.expressoes;

import java.util.List;

import src.raiz.ast.Expressao;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

public class ExpressaoChamadaFuncao extends Expressao {

    private final String nomeFuncao;
    private final List<Expressao> argumentos;

    public ExpressaoChamadaFuncao(Token token, List<Expressao> argumentos) {
        super(token);
        this.nomeFuncao = token.lexema();
        this.argumentos = argumentos;
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
               + getIdentacao(profundidade) + "nomeFuncao: '" + nomeFuncao + "',\n"
               + getIdentacao(profundidade) + "argumentos: " + AstUtil.representacoesArvore(argumentos, profundidade) + "\n"
               + getIdentacao(profundidade - 1) + "}";
    }
}
