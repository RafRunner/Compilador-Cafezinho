package src.raiz.compilador.mips32;

public enum RegistradoresMIPS32 {
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
    T8("$t8"),      // Temporário (não preservado entre chamadas)
    T9("$t9"),      // Temporário (não preservado entre chamadas)
    S0("$s0"),      // Salvo (preservado entre chamadas)
    S1("$s1"),      // Salvo (preservado entre chamadas)
    S2("$s2"),      // Salvo (preservado entre chamadas)
    S3("$s3"),      // Salvo (preservado entre chamadas)
    S4("$s4"),      // Salvo (preservado entre chamadas)
    S5("$s5"),      // Salvo (preservado entre chamadas)
    S6("$s6"),      // Salvo (preservado entre chamadas)
    S7("$s7"),      // Salvo (preservado entre chamadas)
    F0("$f0", true), // Ponto flutuante
    F1("$f1", true), // Ponto flutuante
    F2("$f2", true), // Ponto flutuante
    F3("$f3", true), // Ponto flutuante
    K0("$k0"),      // Reservado para o kernel
    K1("$k1"),      // Reservado para o kernel
    GP("$gp"),      // Ponteiro Global
    SP("$sp"),      // Ponteiro da Stack
    FP("$fp"),      // Frame Pointer
    RA("$ra");      // Endereço de Retorno

    private final String nome;
    private final boolean pontoFlutuante;

    RegistradoresMIPS32(String nome, boolean pontoFlutuante) {
        this.nome = nome;
        this.pontoFlutuante = pontoFlutuante;
    }

    RegistradoresMIPS32(String nome) {
        this(nome, false);
    }

    public String getNome() {
        return nome;
    }

    public boolean isPontoFlutuante() {
        return pontoFlutuante;
    }
}

