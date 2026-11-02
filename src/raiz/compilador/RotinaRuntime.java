package src.raiz.compilador;

// Rotinas de suporte que o compilador inclui no programa gerado quando são necessárias.
// Diferente das funções nativas, não são visíveis na linguagem: são chamadas pelo código gerado
public enum RotinaRuntime {

    // Escreve uma mensagem de erro no stderr e encerra o programa com código de saída 1
    ERRO_EXECUCAO("rt_erro_execucao"),
    // Zera a memória entre $a0 e $a1 (exclusivo), palavra por palavra. Usada nos vetores locais na entrada do bloco;
    // os globais já começam zerados, porque o .space reserva memória zerada
    ZERA_VETOR("rt_zera_vetor");

    public final String label;

    RotinaRuntime(String label) {
        this.label = label;
    }
}
