package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.expressoes.ExpressaoCaractereLiteral;
import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.ast.expressoes.ExpressaoInteiroLiteral;
import src.raiz.ast.expressoes.ExpressaoStringLiteral;
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
}
