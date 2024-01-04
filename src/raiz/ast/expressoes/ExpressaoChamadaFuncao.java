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
        this.nomeFuncao = token.getLexema();
        this.argumentos = argumentos;
    }

    @Override
    public String representacaoString() {
        StringBuilder base = new StringBuilder(nomeFuncao + "(");
        AstUtil.representacoesString(base, argumentos, ", ");
        base.append(")");

        return base.toString();
    }

    @Override
    public String toString() {
        return "ExpressaoChamadaFuncao { nomeFuncao=" + nomeFuncao + ", argumentos=" + argumentos + " }";
    }

}
