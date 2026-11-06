package src.raiz.ast.expressoes;

import src.raiz.ast.NoSintatico;
import src.raiz.ast.TipoVariavel;
import src.raiz.erros.BugCompilador;
import src.raiz.token.Token;

// Expressao é um pedaço de código que produz um valor.
// Coisas como operações lógicas (produzem verdadeiro ou falso),
// uma operação de Infixo (como a soma de dois números, que produz um número), etc
public abstract sealed class Expressao extends NoSintatico
        permits ExpressaoAtribuicao, ExpressaoBinaria, ExpressaoChamadaFuncao, ExpressaoConversao,
        ExpressaoIdentificador, ExpressaoLiteral, ExpressaoTernaria, ExpressaoUnaria, ExpressaoVazio {

    // Tipo do valor produzido pela expressão, preenchido pelo AnalisadorSemantico
    private TipoVariavel tipo;
    // Indica se a expressão no código original estava entre parenteses. Não muda nada
    // na análise ou no backend, pq a estrutura da AST dita a sequência de análise/geração
    private boolean emParenteses;

    public Expressao(Token token) {
        super(token);
        this.emParenteses = false;
    }

    public TipoVariavel getTipo() {
        if (tipo == null) {
            throw new BugCompilador("Expressão sem tipo, a análise semântica não foi feita: " + codigoOriginal());
        }
        return tipo;
    }

    public void setTipo(TipoVariavel tipo) {
        this.tipo = tipo;
    }

    public void setEmParenteses(boolean emParenteses) {
        this.emParenteses = emParenteses;
    }

    @Override
    public String codigoOriginal() {
        if (emParenteses) {
            return "(" + codigoOriginalImpl() + ")";
        } else {
            return codigoOriginalImpl();
        }
    }

    protected abstract String codigoOriginalImpl();
}
