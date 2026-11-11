package src.raiz.compilador.mips32;

// Registradores na ordem da numeração do MIPS ($0-$31 e $f0-$f31), com o papel de cada um na convenção o32
enum RegMIPS32 {
    ZERO("$zero", Banco.INTEIROS, Preservacao.FIXO),            // Registrador zero (sempre 0)
    AT("$at", Banco.INTEIROS, Preservacao.FIXO),                // Registrador de uso reservado pelo assembler
    V0("$v0", Banco.INTEIROS, Preservacao.FIXO),                // Valor de retorno (funções)
    V1("$v1", Banco.INTEIROS, Preservacao.FIXO),                // Valor de retorno (funções)
    A0("$a0", Banco.INTEIROS, Preservacao.FIXO),                // Argumento 1 (funções)
    A1("$a1", Banco.INTEIROS, Preservacao.FIXO),                // Argumento 2 (funções)
    A2("$a2", Banco.INTEIROS, Preservacao.FIXO),                // Argumento 3 (funções)
    A3("$a3", Banco.INTEIROS, Preservacao.FIXO),                // Argumento 4 (funções)
    T0("$t0", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T1("$t1", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T2("$t2", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T3("$t3", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T4("$t4", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T5("$t5", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T6("$t6", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T7("$t7", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    S0("$s0", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S1("$s1", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S2("$s2", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S3("$s3", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S4("$s4", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S5("$s5", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S6("$s6", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    S7("$s7", Banco.INTEIROS, Preservacao.QUEM_E_CHAMADO),      // Salvo (preservado entre chamadas)
    T8("$t8", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    T9("$t9", Banco.INTEIROS, Preservacao.QUEM_CHAMA),          // Temporário (não preservado entre chamadas)
    K0("$k0", Banco.INTEIROS, Preservacao.FIXO),                // Reservado para o kernel
    K1("$k1", Banco.INTEIROS, Preservacao.FIXO),                // Reservado para o kernel
    GP("$gp", Banco.INTEIROS, Preservacao.FIXO),                // Ponteiro Global
    SP("$sp", Banco.INTEIROS, Preservacao.FIXO),                // Ponteiro da Stack
    FP("$fp", Banco.INTEIROS, Preservacao.FIXO),                // Frame Pointer
    RA("$ra", Banco.INTEIROS, Preservacao.FIXO),                // Endereço de Retorno

    // Coprocessador 1. Um dobro ocupa um par par/ímpar ($f0/$f1, $f2/$f3...), referenciado pelo registrador par
    F0("$f0", Banco.FLUTUANTES, Preservacao.FIXO),              // Valor de retorno (funções)
    F1("$f1", Banco.FLUTUANTES, Preservacao.FIXO),              // Valor de retorno (funções)
    F2("$f2", Banco.FLUTUANTES, Preservacao.FIXO),              // Valor de retorno (funções)
    F3("$f3", Banco.FLUTUANTES, Preservacao.FIXO),              // Valor de retorno (funções)
    F4("$f4", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F5("$f5", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F6("$f6", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F7("$f7", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F8("$f8", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F9("$f9", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),        // Temporário (não preservado entre chamadas)
    F10("$f10", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F11("$f11", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F12("$f12", Banco.FLUTUANTES, Preservacao.FIXO),            // Argumento 1 (funções)
    F13("$f13", Banco.FLUTUANTES, Preservacao.FIXO),            // Argumento 1 (funções)
    F14("$f14", Banco.FLUTUANTES, Preservacao.FIXO),            // Argumento 2 (funções)
    F15("$f15", Banco.FLUTUANTES, Preservacao.FIXO),            // Argumento 2 (funções)
    F16("$f16", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F17("$f17", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F18("$f18", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F19("$f19", Banco.FLUTUANTES, Preservacao.QUEM_CHAMA),      // Temporário (não preservado entre chamadas)
    F20("$f20", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F21("$f21", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F22("$f22", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F23("$f23", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F24("$f24", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F25("$f25", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F26("$f26", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F27("$f27", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F28("$f28", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F29("$f29", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F30("$f30", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO),  // Salvo (preservado entre chamadas)
    F31("$f31", Banco.FLUTUANTES, Preservacao.QUEM_E_CHAMADO);  // Salvo (preservado entre chamadas)

    // Quem preserva o registrador numa chamada, pela o32
    enum Preservacao {
        // $t, $f4-$f11 e $f16-$f19: a função chamada pode destruir, e quem chama salva em volta do jal
        QUEM_CHAMA,
        // $s e $f20-$f31: a função chamada salva no prólogo e restaura no epílogo, se usar
        QUEM_E_CHAMADO,
        // Papel fixo (zero, assembler, retorno, argumentos, kernel, ponteiros, $ra): nunca é de uso geral
        FIXO
    }

    private final String nome;
    private final Banco banco;
    private final Preservacao preservacao;

    RegMIPS32(String nome, Banco banco, Preservacao preservacao) {
        this.nome = nome;
        this.banco = banco;
        this.preservacao = preservacao;
    }

    public String getNome() {
        return nome;
    }

    Banco banco() {
        return banco;
    }

    Preservacao preservacao() {
        return preservacao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
