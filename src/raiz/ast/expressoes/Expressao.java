package src.raiz.ast.expressoes;

import src.raiz.ast.NoSintatico;
import src.raiz.ast.TipoVariavel;
import src.raiz.erros.BugCompilador;
import src.raiz.token.Token;

// Expressao é um pedaço de código que produz um valor.
// Coisas como operações lógicas (produzem verdadeiro ou falso),
// uma operação de Infixo (como a soma de dois números, que produz um número), etc
public abstract sealed class Expressao extends NoSintatico
        permits ExpressaoAtribuicao, ExpressaoBinaria, ExpressaoChamadaFuncao, ExpressaoEntreParenteses,
                ExpressaoIdentificador, ExpressaoLiteral, ExpressaoTernaria, ExpressaoUnaria, ExpressaoVazio {

    // Tipo do valor produzido pela expressão, preenchido pelo AnalisadorSemantico
    private TipoVariavel tipo;

    public Expressao(Token token) {
        super(token);
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
}
