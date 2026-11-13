package src.raiz.compilador.mips32;

import org.junit.jupiter.api.Test;

import src.raiz.compilador.mips32.AlocadorRegistradores.Preferencia;
import src.raiz.erros.BugCompilador;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Testes do AlocadorRegistradores: a ordem em que os registradores são entregues, o spill quando eles acabam e o que
// cada chamada e o prólogo precisam salvar
class AlocadorRegistradoresTest {

    // Como num frame com 8 bytes de variáveis locais
    private static final int INICIO_SPILL = -8;

    @Test
    void entregaOTemporarioLivreDeMenorNumero() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);

        Local primeiro = alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        Local segundo = alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        assertEquals(new Local.Registrador(RegMIPS32.T0), primeiro);
        assertEquals(new Local.Registrador(RegMIPS32.T1), segundo);

        alocador.liberar(primeiro);
        assertEquals(new Local.Registrador(RegMIPS32.T0), alocador.alocar(Banco.INTEIROS, Preferencia.CURTA));
    }

    @Test
    void osBancosSaoIndependentes() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);

        assertEquals(new Local.Registrador(RegMIPS32.T0), alocador.alocar(Banco.INTEIROS, Preferencia.CURTA));
        assertEquals(new Local.Registrador(RegMIPS32.F4), alocador.alocar(Banco.FLUTUANTES, Preferencia.CURTA));
        assertEquals(new Local.Registrador(RegMIPS32.F20), alocador.alocar(Banco.FLUTUANTES, Preferencia.LONGA));
    }

    @Test
    void naFaltaDoPreferidoEntregaOOutroTipo() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);

        List<Local> temporarios = alocarVarios(alocador, Banco.INTEIROS, 8);
        assertEquals(new Local.Registrador(RegMIPS32.T7), temporarios.getLast());
        assertEquals(new Local.Registrador(RegMIPS32.S0), alocador.alocar(Banco.INTEIROS, Preferencia.CURTA));
    }

    @Test
    void osRegistradoresDeRecargaNuncaSaoEntregues() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);

        List<Local> inteiros = alocarVarios(alocador, Banco.INTEIROS, 16);
        List<Local> flutuantes = alocarVarios(alocador, Banco.FLUTUANTES, 22);

        for (Local local : concatena(inteiros, flutuantes)) {
            RegMIPS32 registrador = ((Local.Registrador) local).registrador();
            assertNotEquals(RegMIPS32.T8, registrador);
            assertNotEquals(RegMIPS32.T9, registrador);
            assertNotEquals(RegMIPS32.F18, registrador);
            assertNotEquals(RegMIPS32.F19, registrador);
        }
    }

    @Test
    void semRegistradorLivreOValorVaiParaUmSlotAbaixoDasVariaveis() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);
        alocarVarios(alocador, Banco.INTEIROS, 16);

        Local primeiroSlot = alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        Local segundoSlot = alocador.alocar(Banco.FLUTUANTES, Preferencia.CURTA);
        assertEquals(new Local.Memoria(new Deslocamento(RegMIPS32.FP, -12)), primeiroSlot);
        // Os flut ainda têm registradores
        assertEquals(new Local.Registrador(RegMIPS32.F4), segundoSlot);

        assertEquals(new Local.Memoria(new Deslocamento(RegMIPS32.FP, -16)), alocador.alocarSlot());
        assertEquals(8, alocador.tamanhoSpill());
    }

    @Test
    void slotLiberadoEReaproveitadoMasOTamanhoEODoPico() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);

        Local primeiro = alocador.alocarSlot();
        Local segundo = alocador.alocarSlot();
        alocador.liberar(primeiro);
        alocador.liberar(segundo);

        assertEquals(primeiro, alocador.alocarSlot());
        assertEquals(8, alocador.tamanhoSpill());
    }

    @Test
    void liberarOQueNaoEstaOcupadoEBug() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);
        Local temporario = alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        alocador.liberar(temporario);

        assertThrows(BugCompilador.class, () -> alocador.liberar(temporario));
        // Uma variável local mora na memória, mas não é um slot
        assertThrows(
                BugCompilador.class, () -> alocador.liberar(new Local.Memoria(new Deslocamento(RegMIPS32.FP, -4)))
        );
        assertThrows(BugCompilador.class, () -> alocador.liberar(new Local.Global("g_x")));
    }

    @Test
    void validarTodosLiberadosApontaOEsquecido() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);
        alocador.validarTodosLiberados();

        Local esquecido = alocador.alocarSlot();
        assertThrows(BugCompilador.class, alocador::validarTodosLiberados);

        alocador.liberar(esquecido);
        alocador.validarTodosLiberados();
    }

    @Test
    void chamadaSoSalvaOsTemporariosOcupados() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);
        Local t0 = alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        alocador.alocar(Banco.FLUTUANTES, Preferencia.CURTA);
        alocador.alocar(Banco.INTEIROS, Preferencia.LONGA);
        alocador.alocarSlot();
        alocador.liberar(t0);
        alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);

        // O $s0 a função chamada preserva, e o slot sobrevive na memória do frame
        assertEquals(List.of(RegMIPS32.T0, RegMIPS32.T1, RegMIPS32.F4), alocador.temporariosOcupados());
    }

    @Test
    void prologoSalvaOsSalvosUsadosMesmoDepoisDeLiberados() {
        AlocadorRegistradores alocador = new AlocadorRegistradores(INICIO_SPILL);
        Local s0 = alocador.alocar(Banco.INTEIROS, Preferencia.LONGA);
        Local f20 = alocador.alocar(Banco.FLUTUANTES, Preferencia.LONGA);
        alocador.alocar(Banco.INTEIROS, Preferencia.CURTA);
        alocador.liberar(s0);
        alocador.liberar(f20);

        assertEquals(List.of(RegMIPS32.S0, RegMIPS32.F20), alocador.salvosUsados());
    }

    private static List<Local> alocarVarios(AlocadorRegistradores alocador, Banco banco, int quantidade) {
        List<Local> locais = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            locais.add(alocador.alocar(banco, Preferencia.CURTA));
        }
        return locais;
    }

    private static List<Local> concatena(List<Local> a, List<Local> b) {
        List<Local> todos = new ArrayList<>(a);
        todos.addAll(b);
        return todos;
    }
}
