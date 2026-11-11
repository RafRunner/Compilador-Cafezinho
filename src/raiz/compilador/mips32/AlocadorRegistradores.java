package src.raiz.compilador.mips32;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import src.raiz.erros.BugCompilador;

// Entrega registradores para os temporários das expressões de uma função. É criado um novo para cada função (e para o
// main): todo alocador nasce limpo, porque pela convenção a função pode destruir qualquer $t (quem chamou salvou os
// seus) e usar qualquer $s (desde que o epílogo o restaure).
//
// Quando os registradores acabam, o valor novo mora num slot da área de spill (spill na produção): o Local devolvido
// nunca muda de lugar, então quem já recebeu um Local nunca o vê ficar inválido. A área fica a offsets fixos do $fp,
// logo abaixo das variáveis locais, e não no $sp, que se move durante as chamadas
class AlocadorRegistradores {

    // Por quanto tempo o valor fica vivo. Decide se o alocador tenta primeiro um temporário ou um salvo; na falta do
    // preferido, entrega o outro
    enum Preferencia {
        // Quase nunca cruza um jal (temporário de expressão): um $t não custa nada
        CURTA,
        // Atravessa chamadas (variável usada num laço com chamada, recursão): um $s custa um sw/lw no prólogo e no
        // epílogo, em vez de ser salvo em volta de cada chamada
        LONGA
    }

    // Os EnumSet iteram na ordem de declaração do RegMIPS32, que é a do número do registrador: pegar o primeiro livre
    // é pegar o de menor número

    // Caller-saved: quem chama salva os ocupados em volta do jal
    private static final Set<RegMIPS32> INTEIROS_TEMPORARIOS = EnumSet.range(RegMIPS32.T0, RegMIPS32.T7);

    // Callee-saved: o prólogo e o epílogo salvam os que forem entregues alguma vez na função
    private static final Set<RegMIPS32> INTEIROS_SALVOS = EnumSet.range(RegMIPS32.S0, RegMIPS32.S7);

    // Nunca entregues: servem para quem consome um valor que está na memória, e para quem produz um valor que vai
    // para a memória. Dois, porque uma binária pode ter os dois operandos na memória
    private static final List<RegMIPS32> INTEIROS_RECARGA = List.of(RegMIPS32.T8, RegMIPS32.T9);

    // $f0-$f3 são de retorno e $f12-$f15 de argumentos
    private static final Set<RegMIPS32> FLUTUANTES_TEMPORARIOS = EnumSet.of(
            RegMIPS32.F4, RegMIPS32.F5, RegMIPS32.F6, RegMIPS32.F7,
            RegMIPS32.F8, RegMIPS32.F9, RegMIPS32.F10, RegMIPS32.F11,
            RegMIPS32.F16, RegMIPS32.F17
    );

    private static final Set<RegMIPS32> FLUTUANTES_SALVOS = EnumSet.range(RegMIPS32.F20, RegMIPS32.F31);

    private static final List<RegMIPS32> FLUTUANTES_RECARGA = List.of(RegMIPS32.F18, RegMIPS32.F19);

    private static final Map<Banco, Set<RegMIPS32>> TEMPORARIOS = Map.of(
            Banco.INTEIROS, INTEIROS_TEMPORARIOS,
            Banco.FLUTUANTES, FLUTUANTES_TEMPORARIOS
    );

    private static final Map<Banco, Set<RegMIPS32>> SALVOS = Map.of(
            Banco.INTEIROS, INTEIROS_SALVOS,
            Banco.FLUTUANTES, FLUTUANTES_SALVOS
    );

    private static final Map<Banco, List<RegMIPS32>> RECARGA = Map.of(
            Banco.INTEIROS, INTEIROS_RECARGA,
            Banco.FLUTUANTES, FLUTUANTES_RECARGA
    );

    // Offset em relação ao $fp de onde a área de spill começa (ela cresce para baixo, como as variáveis locais)
    private final int inicioSpill;

    // Os registradores entregues e ainda não liberados, dos dois bancos e dos dois tipos
    private final Set<RegMIPS32> ocupados = EnumSet.noneOf(RegMIPS32.class);

    // Os callee-saved entregues alguma vez nesta função, mesmo que já liberados: o prólogo e o epílogo os salvam e
    // restauram. Só cresce
    private final Set<RegMIPS32> salvosUsados = EnumSet.noneOf(RegMIPS32.class);

    // Os slots de spill ocupados agora (estado) e o máximo ocupado ao mesmo tempo (histórico, que dá o tamanho da
    // área). Um slot é uma palavra e não tem banco: cabe um int ou os bits de um flut
    private final BitSet slotsOcupados = new BitSet();
    private int maximoSlots = 0;

    // inicioSpill: onde termina o espaço das variáveis locais, normalmente -frame.getTamanho()
    AlocadorRegistradores(int inicioSpill) {
        this.inicioSpill = inicioSpill;
    }

    // O primeiro livre do tipo preferido; na falta dele, do outro. Um salvo entregue entra em salvosUsados. Sem
    // nenhum registrador livre, um slot de spill
    Local alocar(Banco banco, Preferencia preferencia) {
        Set<RegMIPS32> preferidos = preferencia == Preferencia.CURTA ? TEMPORARIOS.get(banco) : SALVOS.get(banco);
        Set<RegMIPS32> outros = preferencia == Preferencia.CURTA ? SALVOS.get(banco) : TEMPORARIOS.get(banco);

        Optional<RegMIPS32> livre = primeiroLivre(preferidos).or(() -> primeiroLivre(outros));
        if (livre.isEmpty()) {
            return alocarSlot();
        }

        RegMIPS32 alocado = livre.get();
        ocupados.add(alocado);
        if (alocado.preservacao() == RegMIPS32.Preservacao.QUEM_E_CHAMADO) {
            salvosUsados.add(alocado);
        }

        return new Local.Registrador(alocado);
    }

    // Um slot de spill, sem tentar os registradores
    Local alocarSlot() {
        int slot = slotsOcupados.nextClearBit(0);
        slotsOcupados.set(slot);
        maximoSlots = Math.max(maximoSlots, slot + 1);
        return new Local.Memoria(new Deslocamento(RegMIPS32.FP, offsetSlot(slot)));
    }

    // Liberar o que não é um registrador ou slot ocupado deste alocador (uma variável, um registrador fixo, um
    // temporário já liberado) é BugCompilador
    void liberar(Local local) {
        switch (local) {
            case Local.Registrador(RegMIPS32 registrador) -> {
                if (!ocupados.remove(registrador)) {
                    throw new BugCompilador("Registrador " + registrador + " não está alocado");
                }
            }
            case Local.Memoria(Deslocamento deslocamento) -> {
                int slot = slotDe(deslocamento);
                if (slot < 0 || !slotsOcupados.get(slot)) {
                    throw new BugCompilador("Endereço " + deslocamento + " não é um slot de spill ocupado");
                }
                slotsOcupados.clear(slot);
            }
            case Local.Global global -> throw new BugCompilador("Variável global não pode ser liberada: " + global);
        }
    }

    // Os caller-saved ocupados, na ordem do número: é o que uma chamada salva antes do jal e restaura depois. Os salvos
    // ocupados ficam de fora, porque a função chamada já os preserva, e os slots também, porque a memória do frame
    // sobrevive à chamada
    List<RegMIPS32> temporariosOcupados() {
        return ocupados.stream()
                .filter(r -> r.preservacao() == RegMIPS32.Preservacao.QUEM_CHAMA)
                .toList();
    }

    // Para o prólogo e o epílogo, gerados depois do corpo
    List<RegMIPS32> salvosUsados() {
        return List.copyOf(salvosUsados);
    }

    // Quantos bytes a área de spill ocupa: o máximo de slots ocupados ao mesmo tempo
    int tamanhoSpill() {
        return maximoSlots * FrameMIPS32.TAMANHO_PALAVRA;
    }

    // Roda no fim de cada comando: faz o papel da pilha balanceada, e um liberar esquecido aparece no comando que o
    // esqueceu
    void validarTodosLiberados() {
        if (!ocupados.isEmpty() || !slotsOcupados.isEmpty()) {
            throw new BugCompilador(
                    "Temporários não liberados no fim do comando: " + ocupados + ", slots " + slotsOcupados
            );
        }
    }

    // O i-ésimo (0 ou 1) registrador de recarga do banco
    static RegMIPS32 recarga(Banco banco, int i) {
        return RECARGA.get(banco).get(i);
    }

    private Optional<RegMIPS32> primeiroLivre(Set<RegMIPS32> registradores) {
        return registradores.stream().filter(r -> !ocupados.contains(r)).findFirst();
    }

    private int offsetSlot(int slot) {
        return inicioSpill - FrameMIPS32.TAMANHO_PALAVRA * (slot + 1);
    }

    // O índice do slot que fica no deslocamento, ou -1 se ele não está na área de spill
    private int slotDe(Deslocamento deslocamento) {
        int distancia = inicioSpill - deslocamento.offset();
        if (deslocamento.base() != RegMIPS32.FP || distancia <= 0 || distancia % FrameMIPS32.TAMANHO_PALAVRA != 0) {
            return -1;
        }
        return distancia / FrameMIPS32.TAMANHO_PALAVRA - 1;
    }
}
