package src.raiz.compilador.mips32;

// Rotinas de suporte que o backend inclui no programa gerado, uma única vez e só quando são usadas.
// Há dois tipos, diferenciados pelo prefixo do label:
//   rt_  runtime: chamadas implicitamente pelo código gerado, nunca escritas pelo programador
//   lib_ biblioteca: implementam funções nativas que não valem a pena gerar no lugar da chamada
enum RotinaMIPS32 {

    // Escreve uma mensagem no stderr e encerra o programa com código de saída 1.
    // $a1: endereço da mensagem (bytes UTF-8), $a2: tamanho da mensagem em bytes
    ERRO_EXECUCAO("rt_erro_execucao"),

    // Número aleatório em [0, $a0), resultado em $v0. Retorna 0 se $a0 <= 0
    RAND("lib_rand");

    final String label;

    RotinaMIPS32(String label) {
        this.label = label;
    }
}
