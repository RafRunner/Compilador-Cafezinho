package src.raiz.compilador.mips32;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import src.raiz.erros.BugCompilador;

// Entrega registradores para os temporários das expressões de uma função. É criado um novo para cada função (e para o
// main): todo alocador nasce limpo, porque pela convenção a função pode destruir qualquer $t (quem chamou salvou os
// seus) e usar qualquer $s (desde que o epílogo o restaure)
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

    // Caller-saved: quem chama salva os ocupados em volta do jal. Os $t0-$t3 ficam de fora enquanto os nós antigos os
    // usam de rascunho, e entram quando o adaptador sair
    private static final Set<RegMIPS32> INTEIROS_TEMPORARIOS = EnumSet.range(RegMIPS32.T4, RegMIPS32.T7);

    // Callee-saved: o prólogo e o epílogo salvam os que forem entregues alguma vez na função. O $s0 fica de fora
    // enquanto for rascunho (desempilharEmS0)
    private static final Set<RegMIPS32> INTEIROS_SALVOS = EnumSet.range(RegMIPS32.S1, RegMIPS32.S7);

    // Nunca entregues: servem para quem consome um valor que está na memória. Dois, porque uma binária pode ter os
    // dois operandos na memória
    private static final List<RegMIPS32> INTEIROS_RECARGA = List.of(RegMIPS32.T8, RegMIPS32.T9);

    // $f0-$f3 são de retorno ($f0 e $f1 também de rascunho) e $f12-$f15 de argumentos ($f12 também do escreva)
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

    // Os registradores entregues e ainda não liberados, dos dois bancos e dos dois tipos
    private final Set<RegMIPS32> ocupados = EnumSet.noneOf(RegMIPS32.class);

    // Os callee-saved entregues alguma vez nesta função, mesmo que já liberados: o prólogo e o epílogo os salvam e
    // restauram. Só cresce
    private final Set<RegMIPS32> salvosUsados = EnumSet.noneOf(RegMIPS32.class);

    // O primeiro livre do tipo preferido; na falta dele, do outro. Um salvo entregue entra em salvosUsados. Sem
    // nenhum livre, por enquanto é BugCompilador; com o spill, devolve um Local.Memoria num slot novo
    Local alocar(Banco banco, Preferencia preferencia) {
        Set<RegMIPS32> disponiveis = switch (preferencia) {
            case CURTA -> {
                Set<RegMIPS32> curtos = livres(TEMPORARIOS.get(banco));
                if (curtos.isEmpty()) {
                    yield livres(SALVOS.get(banco));
                } else {
                    yield curtos;
                }
            }
            case LONGA -> {
                Set<RegMIPS32> longos = livres(SALVOS.get(banco));
                if (longos.isEmpty()) {
                    yield livres(TEMPORARIOS.get(banco));
                } else {
                    yield longos;
                }
            }
        };

        Optional<RegMIPS32> disponivel = disponiveis.stream().findFirst();

        // Temporário, por enquanto só alocamos registradores
        if (disponivel.isEmpty()) {
            throw new BugCompilador("Somente alocação de registradores por enquanto");
        }

        RegMIPS32 alocado = disponivel.get();
        ocupados.add(alocado);
        if (alocado.preservacao() == RegMIPS32.Preservacao.QUEM_E_CHAMADO) {
            salvosUsados.add(alocado);
        }

        return new Local.Registrador(alocado);
    }

    // Liberar o que não é um registrador ocupado deste alocador (uma variável, um registrador fixo, um temporário já
    // liberado) é BugCompilador
    void liberar(Local local) {
        switch (local) {
            case Local.Registrador(RegMIPS32 registrador) -> {
                if (!ocupados.contains(registrador)) {
                    throw new BugCompilador("Registrador " + registrador + " não está alocado!");
                }
                ocupados.remove(registrador);
            }
            case Local.Memoria(Deslocamento deslocamento) -> {
                throw new BugCompilador("Somente alocação de registradores por enquanto");
            }
            case Local.Global(String label) -> {
                throw new BugCompilador("Variáveis Gloabais não podem ser liberadas!");
            }
        }
    }

    // Os caller-saved ocupados, na ordem do número: é o que uma chamada salva antes do jal e restaura depois. Os salvos
    // ocupados ficam de fora, porque a função chamada já os preserva
    Set<RegMIPS32> temporariosOcupados() {
        return ocupados.stream()
                .filter(r -> r.preservacao() == RegMIPS32.Preservacao.QUEM_CHAMA)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RegMIPS32.class)));
    }

    // Para o prólogo e o epílogo, gerados depois do corpo
    Set<RegMIPS32> salvosUsados() {
        return salvosUsados.isEmpty()
                ? EnumSet.noneOf(RegMIPS32.class)
                : EnumSet.copyOf(salvosUsados);
    }

    // Roda no fim de cada comando: faz o papel da pilha balanceada, e um liberar esquecido aparece no comando que o
    // esqueceu
    void validarTodosLiberados() {
        if (!ocupados.isEmpty()) {
            throw new BugCompilador("O bloco deveria ter liberado todos os registradores");
        }
    }

    // O i-ésimo (0 ou 1) registrador de recarga do banco, para quem consome um valor que está na memória
    static RegMIPS32 recarga(Banco banco, int i) {
        return RECARGA.get(banco).get(i);
    }

    private Set<RegMIPS32> livres(Set<RegMIPS32> banco) {
        return banco.stream()
                .filter(r -> !ocupados.contains(r))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RegMIPS32.class)));
    }

    // Com o spill:
    // Local alocarSlot() - um slot de spill para a chamada guardar um temporário ocupado
    // int tamanhoSpill() - o máximo de slots ocupados ao mesmo tempo, para o prólogo reservar o frame
}
