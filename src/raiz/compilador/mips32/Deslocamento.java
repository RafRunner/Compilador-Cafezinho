package src.raiz.compilador.mips32;

public record Deslocamento(RegMIPS32 base, int offset) {

    @Override
    public String toString() {
        return offset + "(" + base.getNome() + ")";
    }
}
