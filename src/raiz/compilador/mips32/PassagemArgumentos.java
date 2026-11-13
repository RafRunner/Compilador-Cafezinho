package src.raiz.compilador.mips32;

// Onde um argumento fica na área de argumentos (ver AreaArgumentos). Todo argumento tem um slot, com o offset em
// relação ao início da área; os slots da home area também chegam num registrador
sealed interface PassagemArgumentos {

    abstract int offset();

    record Registrador(RegMIPS32 registrador, int offset) implements PassagemArgumentos {
    }

    record Pilha(int offset) implements PassagemArgumentos {
    }
}
