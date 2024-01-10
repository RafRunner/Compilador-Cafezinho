package src.raiz.compilador;

import src.raiz.ast.DeclaracaoFuncoesEVariaveis;

public interface VisitadorDeNos {

    void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node);
}
