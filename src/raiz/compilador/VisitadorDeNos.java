package src.raiz.compilador;

import src.raiz.ast.DeclaracaoFuncoesEVariaveis;
import src.raiz.ast.Programa;

public interface VisitadorDeNos {

    void visitarPorgrama(Programa programa);

    void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node);
}
