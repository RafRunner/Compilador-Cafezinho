package src.raiz.ast.declaracoes;

import src.raiz.token.Token;

// O que pode aparecer no nível global do programa, antes do bloco programa: variáveis e funções
public sealed interface DeclaracaoGlobal permits DeclaracaoDeVariavel, DeclaracaoFuncao {

    Token getToken();

    String codigoOriginal();
}
