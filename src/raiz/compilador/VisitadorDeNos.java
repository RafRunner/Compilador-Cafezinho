package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.ComandoEscreva;
import src.raiz.ast.comandos.ComandoNovalinha;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.tabeladesimbolos.TabelaDeSimbolos;

public interface VisitadorDeNos {

    void visitarPorgrama();

    void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node);

    void visitarBlocoPrograma(BlocoPrograma blocoPrograma);

    void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaPai);

    TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo);

    void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco);

    TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela);

    void visitarComandoNovalinha(ComandoNovalinha comandoNovalinha);

    TipoVariavel visitarExpressaoSoma(ExpressaoMais expressaoMais, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoSubtracao(ExpressaoMenos expressaoMenos, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoVezes(ExpressaoVezes expressaoVezes, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoDivisao(ExpressaoDivisao expressaoDivisao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoE(ExpressaoE expressaoE, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoOu(ExpressaoOu expressaoOu, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoIgual(ExpressaoIgual expressaoIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMaior(ExpressaoMaior expressaoMaior, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMaiorIgual(ExpressaoMaiorIgual expressaoMaiorIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMenor(ExpressaoMenor expressaoMenor, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMenorIgual(ExpressaoMenorIgual expressaoMenorIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoResto(ExpressaoResto expressaoResto, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela);

    void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela);
}
