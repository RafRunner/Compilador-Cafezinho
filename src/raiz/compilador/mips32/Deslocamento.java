package src.raiz.compilador.mips32;

public record Deslocamento(RegistradoresMIPS32 base, int offset) {

    @Override
    public String toString() {
        return offset + "(" + base.getNome() + ")";
    }
}
