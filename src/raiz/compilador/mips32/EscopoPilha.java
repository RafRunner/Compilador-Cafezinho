package src.raiz.compilador.mips32;

// Modelo, em tempo de compilação, da pilha de execução de um escopo. As variáveis locais são acessadas
// relativas ao $sp, que muda a cada empilhamento, então é preciso acompanhar quanto a pilha cresceu
class EscopoPilha {

    private final int offsetInicial;
    private int offset;

    EscopoPilha() {
        this(0);
    }

    private EscopoPilha(int offsetInicial) {
        this.offsetInicial = offsetInicial;
        this.offset = offsetInicial;
    }

    EscopoPilha criaBlocoInterno() {
        return new EscopoPilha(offset);
    }

    int getOffset() {
        return offset;
    }

    void alteraOffset(int valor) {
        offset += valor;
    }

    // Distância do $sp atual até uma variável que foi empilhada quando a pilha tinha offsetVariavel
    int getOffsetStack(int offsetVariavel) {
        return offset - offsetVariavel - 4;
    }

    int getDiferencaOffset() {
        return offset - offsetInicial;
    }
}
