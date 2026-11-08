package src.raiz.compilador.mips32;

import java.util.ArrayList;
import java.util.List;

import src.raiz.ast.ParametroFuncao;
import src.raiz.compilador.mips32.LocalArgumento.Pilha;
import src.raiz.compilador.mips32.LocalArgumento.Registrador;

// A área de argumentos de uma chamada, na convenção o32. Quem chama reserva a área no topo da pilha, e ela é montada
// como uma struct: cada argumento ocupa o slot seguinte, na ordem. Os slots dos primeiros 16 bytes (a home area)
// chegam em registrador, e a função chamada os salva ali no prólogo; os demais quem chama já grava na pilha. Assim,
// dentro da função, todos os parâmetros ficam em sequência na memória
record AreaArgumentos(List<LocalArgumento> locais, int tamanho) {

    static final int TAMANHO_HOME_AREA = 16;

    private static final List<RegMIPS32> REGISTRADORES = List.of(RegMIPS32.A0, RegMIPS32.A1, RegMIPS32.A2, RegMIPS32.A3);

    static AreaArgumentos de(List<ParametroFuncao> parametros) {
        List<LocalArgumento> locais = new ArrayList<>(parametros.size());
        int offset = 0;
        // Os $f só são usados pelos dois primeiros argumentos, e só se a lista começar com flut
        boolean soFlutAteAqui = true;

        for (int i = 0; i < parametros.size(); i++) {
            ParametroFuncao parametro = parametros.get(i);
            soFlutAteAqui &= isFlutuante(parametro);

            if (soFlutAteAqui && i < 2) {
                // O $a do slot fica sem uso
                locais.add(new Registrador(i == 0 ? RegMIPS32.F12 : RegMIPS32.F14, offset));
            } else if (offset < TAMANHO_HOME_AREA) {
                // O registrador sai do slot, não do índice do argumento. Um flut aqui vai como bits
                locais.add(new Registrador(REGISTRADORES.get(offset / FrameMIPS32.TAMANHO_PALAVRA), offset));
            } else {
                locais.add(new Pilha(offset));
            }
            offset += FrameMIPS32.tamanhoVariavel(parametro.getSimbolo());
        }

        // A home area é reservada mesmo que a função tenha menos de 4 argumentos
        return new AreaArgumentos(List.copyOf(locais), Math.max(offset, TAMANHO_HOME_AREA));
    }

    // Um vetor é passado como endereço, mesmo sendo de flut
    private static boolean isFlutuante(ParametroFuncao parametro) {
        return !parametro.isVetor() && parametro.getSimbolo().getTipo().isFlutuante();
    }
}
