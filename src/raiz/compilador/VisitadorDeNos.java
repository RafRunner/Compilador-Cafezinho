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

    void visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo);

    void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco);

    void visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabelaDeSimbolos);

    void visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos);

    void visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos);

    void visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabelaDeSimbolos);
}
