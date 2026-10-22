package src.raiz.semantica;

import src.raiz.token.Token;

// Uma entrada da tabela de símbolos: algo que tem nome no programa
public sealed interface Simbolo permits SimboloVariavel, SimboloInvocavel {

    String getNome();

    // Usada nas mensagens de erro, ex: "A variável local 'x' já foi declarada"
    String getDescricao();

    // Onde o símbolo foi declarado. Funções nativas não têm declaração no código fonte
    Token getToken();
}
