package src.raiz.compilador.mips32;

// Modelo, em tempo de compilação, de quanto a pilha cresceu dentro de um escopo. As variáveis são acessadas em
// relação ao $fp, então isto só serve para saber quanto liberar no fim de cada bloco
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

    int getDiferencaOffset() {
        return offset - offsetInicial;
    }
}
