package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.expressoes.ExpressaoIdentificador;
import src.raiz.compilador.tabeladesimbolos.TabelaDeSimbolos;

public interface VisitadorDeNos {

    void visitarPorgrama();

    void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node);

    void visitarBlocoPrograma(BlocoPrograma blocoPrograma);

    void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaPai);

    void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco);

    void visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabelaDeSimbolos);
}
