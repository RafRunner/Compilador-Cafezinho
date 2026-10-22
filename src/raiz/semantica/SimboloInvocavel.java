package src.raiz.semantica;

// Símbolos que podem ser chamados: funções do programa e funções nativas
public sealed interface SimboloInvocavel extends Simbolo permits SimboloFuncao, SimboloFuncaoNativa {
}
