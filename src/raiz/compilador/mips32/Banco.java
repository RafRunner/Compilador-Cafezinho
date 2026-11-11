package src.raiz.compilador.mips32;

import src.raiz.ast.TipoVariavel;

// O conjunto de registradores de onde um valor sai: os de uso geral ou os do coprocessador 1. Decide as instruções
// (lw ou lwc1, add ou add.s, e quando precisa de mtc1/mfc1)
enum Banco {
    INTEIROS,
    FLUTUANTES;

    // Só um flut escalar vai nos $f. Um vetor, mesmo de flut, é um endereço; car e string também ficam nos inteiros
    static Banco de(TipoVariavel tipo, boolean isVetor) {
        return isVetor ? INTEIROS : de(tipo);
    }

    // valores de expressão
    static Banco de(TipoVariavel tipo) {
        return tipo.isFlutuante() ? FLUTUANTES : INTEIROS;
    }

}
