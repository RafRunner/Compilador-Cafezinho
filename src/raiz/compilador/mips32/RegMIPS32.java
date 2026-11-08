package src.raiz.compilador.mips32;

// Registradores na ordem da numeração do MIPS ($0-$31 e $f0-$f31), com o papel de cada um na convenção o32
enum RegMIPS32 {
    ZERO("$zero"),  // Registrador zero (sempre 0)
    AT("$at"),      // Registrador de uso reservado pelo assembler
    V0("$v0"),      // Valor de retorno (funções)
    V1("$v1"),      // Valor de retorno (funções)
    A0("$a0"),      // Argumento 1 (funções)
    A1("$a1"),      // Argumento 2 (funções)
    A2("$a2"),      // Argumento 3 (funções)
    A3("$a3"),      // Argumento 4 (funções)
    T0("$t0"),      // Temporário (não preservado entre chamadas)
    T1("$t1"),      // Temporário (não preservado entre chamadas)
    T2("$t2"),      // Temporário (não preservado entre chamadas)
    T3("$t3"),      // Temporário (não preservado entre chamadas)
    T4("$t4"),      // Temporário (não preservado entre chamadas)
    T5("$t5"),      // Temporário (não preservado entre chamadas)
    T6("$t6"),      // Temporário (não preservado entre chamadas)
    T7("$t7"),      // Temporário (não preservado entre chamadas)
    S0("$s0"),      // Salvo (preservado entre chamadas)
    S1("$s1"),      // Salvo (preservado entre chamadas)
    S2("$s2"),      // Salvo (preservado entre chamadas)
    S3("$s3"),      // Salvo (preservado entre chamadas)
    S4("$s4"),      // Salvo (preservado entre chamadas)
    S5("$s5"),      // Salvo (preservado entre chamadas)
    S6("$s6"),      // Salvo (preservado entre chamadas)
    S7("$s7"),      // Salvo (preservado entre chamadas)
    T8("$t8"),      // Temporário (não preservado entre chamadas)
    T9("$t9"),      // Temporário (não preservado entre chamadas)
    K0("$k0"),      // Reservado para o kernel
    K1("$k1"),      // Reservado para o kernel
    GP("$gp"),      // Ponteiro Global
    SP("$sp"),      // Ponteiro da Stack
    FP("$fp"),      // Frame Pointer
    RA("$ra"),      // Endereço de Retorno

    // Coprocessador 1. Um dobro ocupa um par par/ímpar ($f0/$f1, $f2/$f3...), referenciado pelo registrador par
    F0("$f0", true),    // Valor de retorno (funções)
    F1("$f1", true),    // Valor de retorno (funções)
    F2("$f2", true),    // Valor de retorno (funções)
    F3("$f3", true),    // Valor de retorno (funções)
    F4("$f4", true),    // Temporário (não preservado entre chamadas)
    F5("$f5", true),    // Temporário (não preservado entre chamadas)
    F6("$f6", true),    // Temporário (não preservado entre chamadas)
    F7("$f7", true),    // Temporário (não preservado entre chamadas)
    F8("$f8", true),    // Temporário (não preservado entre chamadas)
    F9("$f9", true),    // Temporário (não preservado entre chamadas)
    F10("$f10", true),  // Temporário (não preservado entre chamadas)
    F11("$f11", true),  // Temporário (não preservado entre chamadas)
    F12("$f12", true),  // Argumento 1 (funções)
    F13("$f13", true),  // Argumento 1 (funções)
    F14("$f14", true),  // Argumento 2 (funções)
    F15("$f15", true),  // Argumento 2 (funções)
    F16("$f16", true),  // Temporário (não preservado entre chamadas)
    F17("$f17", true),  // Temporário (não preservado entre chamadas)
    F18("$f18", true),  // Temporário (não preservado entre chamadas)
    F19("$f19", true),  // Temporário (não preservado entre chamadas)
    F20("$f20", true),  // Salvo (preservado entre chamadas)
    F21("$f21", true),  // Salvo (preservado entre chamadas)
    F22("$f22", true),  // Salvo (preservado entre chamadas)
    F23("$f23", true),  // Salvo (preservado entre chamadas)
    F24("$f24", true),  // Salvo (preservado entre chamadas)
    F25("$f25", true),  // Salvo (preservado entre chamadas)
    F26("$f26", true),  // Salvo (preservado entre chamadas)
    F27("$f27", true),  // Salvo (preservado entre chamadas)
    F28("$f28", true),  // Salvo (preservado entre chamadas)
    F29("$f29", true),  // Salvo (preservado entre chamadas)
    F30("$f30", true),  // Salvo (preservado entre chamadas)
    F31("$f31", true);  // Salvo (preservado entre chamadas)

    private final String nome;
    private final boolean pontoFlutuante;

    RegMIPS32(String nome, boolean pontoFlutuante) {
        this.nome = nome;
        this.pontoFlutuante = pontoFlutuante;
    }

    RegMIPS32(String nome) {
        this(nome, false);
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }

    public boolean isPontoFlutuante() {
        return pontoFlutuante;
    }
}
