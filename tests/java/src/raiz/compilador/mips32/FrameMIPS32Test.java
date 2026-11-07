package src.raiz.compilador.mips32;

import org.junit.jupiter.api.Test;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.Programa;
import src.raiz.ast.Variavel;
import src.raiz.ast.comandos.Comando;
import src.raiz.ast.comandos.ComandoBloco;
import src.raiz.ast.comandos.ComandoEnquanto;
import src.raiz.ast.comandos.ComandoSe;
import src.raiz.ast.declaracoes.BlocoDeclaracoes;
import src.raiz.ast.declaracoes.DeclaracaoFuncao;
import src.raiz.erros.BugCompilador;
import src.raiz.generated.Lexer;
import src.raiz.generated.Parser;
import src.raiz.semantica.AnalisadorSemantico;
import src.raiz.sintatico.AdaptadorLexer;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Testes do layout do frame: o offset de cada parâmetro e variável local em relação ao $fp e o tamanho que o prólogo
// reserva. Os programas passam pela análise semântica antes, para garantir que são cafezinho válido
class FrameMIPS32Test {

    @Test
    void programaSemVariaveisNaoReservaEspaco() {
        Programa programa = analisa("programa { novalinha; }");

        assertEquals(0, FrameMIPS32.dePrograma(programa.getBlocoPrograma()).getTamanho());
    }

    @Test
    void parametrosFicamAcimaDoFpNaOrdemEmQueForamEmpilhados() {
        Programa programa = analisa("""
                vazio f(int a, int b, int c) {
                    novalinha;
                }
                programa { novalinha; }
                """);
        DeclaracaoFuncao f = funcao(programa, "f");
        FrameMIPS32 frame = FrameMIPS32.deFuncao(f);

        // O último argumento empilhado fica logo acima do $ra salvo, em 8($fp)
        assertEquals(16, frame.offset(parametro(f, "a")));
        assertEquals(12, frame.offset(parametro(f, "b")));
        assertEquals(8, frame.offset(parametro(f, "c")));
        assertEquals(0, frame.getTamanho(), "parâmetros ficam no espaço reservado por quem chama");
    }

    @Test
    void locaisComecamLogoAbaixoDoFpMesmoComParametros() {
        Programa programa = analisa("""
                vazio f(int p) {
                    int x, y;
                    car z;
                    novalinha;
                }
                programa { novalinha; }
                """);
        DeclaracaoFuncao f = funcao(programa, "f");
        FrameMIPS32 frame = FrameMIPS32.deFuncao(f);

        assertEquals(8, frame.offset(parametro(f, "p")));
        assertEquals(-4, frame.offset(variavel(f.getCorpo(), "x")));
        assertEquals(-8, frame.offset(variavel(f.getCorpo(), "y")));
        assertEquals(-12, frame.offset(variavel(f.getCorpo(), "z")), "car local ocupa 4 bytes, como as demais");
        assertEquals(12, frame.getTamanho());
    }

    @Test
    void vetorOcupaOEspacoDosItensQuandoLocal() {
        Programa programa = analisa("""
                programa {
                    int v[10], w;
                    novalinha;
                }
                """);
        BlocoDeclaracoes corpo = programa.getBlocoPrograma().getBlocoDeclaracoes();
        FrameMIPS32 frame = FrameMIPS32.dePrograma(programa.getBlocoPrograma());

        // Os 10 elementos ficam no frame, e o offset do vetor é o do v[0], o endereço mais baixo da região
        assertEquals(-40, frame.offset(variavel(corpo, "v")));
        assertEquals(-44, frame.offset(variavel(corpo, "w")));
        assertEquals(44, frame.getTamanho());
    }

    @Test
    void vetorGlobalNaoFicaNoFrame() {
        // A local v esconde a global v. Os tamanhos diferentes mostram que só a local ocupa o frame
        Programa programa = analisa("""
                int v[10];
                programa {
                    int v[3], w;
                    novalinha;
                }
                """);
        BlocoDeclaracoes corpo = programa.getBlocoPrograma().getBlocoDeclaracoes();
        FrameMIPS32 frame = FrameMIPS32.dePrograma(programa.getBlocoPrograma());
        Variavel vetorGlobal = global(programa, "v");

        assertEquals(-12, frame.offset(variavel(corpo, "v")));
        assertEquals(-16, frame.offset(variavel(corpo, "w")));
        assertEquals(16, frame.getTamanho(), "a global fica no segmento de dados, não no frame");

        BugCompilador erro = assertThrows(BugCompilador.class, () -> frame.offset(vetorGlobal));
        assertTrue(erro.getMessage().contains("'v'"), erro.getMessage());
        assertEquals(40, FrameMIPS32.tamanhoVariavel(vetorGlobal.getSimbolo()));
    }

    @Test
    void blocosIrmaosReaproveitamOMesmoEspaco() {
        Programa programa = analisa("""
                programa {
                    int a;
                    { int b; novalinha; }
                    { int c, d; novalinha; }
                }
                """);
        BlocoDeclaracoes corpo = programa.getBlocoPrograma().getBlocoDeclaracoes();
        FrameMIPS32 frame = FrameMIPS32.dePrograma(programa.getBlocoPrograma());

        assertEquals(-4, frame.offset(variavel(corpo, "a")));
        assertEquals(-8, frame.offset(variavel(corpo, "b")));
        assertEquals(-8, frame.offset(variavel(corpo, "c")), "c começa onde b começava");
        assertEquals(-12, frame.offset(variavel(corpo, "d")));
        assertEquals(12, frame.getTamanho(), "o maior dos irmãos, não a soma");
    }

    @Test
    void blocosAninhadosAcumulam() {
        Programa programa = analisa("""
                programa {
                    int x;
                    {
                        int y;
                        {
                            int z;
                            novalinha;
                        }
                    }
                }
                """);
        BlocoDeclaracoes corpo = programa.getBlocoPrograma().getBlocoDeclaracoes();
        FrameMIPS32 frame = FrameMIPS32.dePrograma(programa.getBlocoPrograma());

        assertEquals(-4, frame.offset(variavel(corpo, "x")));
        assertEquals(-8, frame.offset(variavel(corpo, "y")));
        assertEquals(-12, frame.offset(variavel(corpo, "z")));
        assertEquals(12, frame.getTamanho());
    }

    @Test
    void blocosDentroDeSeSenaoEEnquantoContam() {
        // Inclui os casos em que o bloco não é filho direto: senao se e um se dentro de um enquanto
        Programa programa = analisa("""
                programa {
                    int a;
                    a = 1;
                    se (a) entao { int b; novalinha; }
                    senao se (a) entao { int c, d; novalinha; }
                    senao { int i; novalinha; }
                    enquanto (a) execute se (a) entao { int f, g, h; a = 0; }
                }
                """);
        BlocoDeclaracoes corpo = programa.getBlocoPrograma().getBlocoDeclaracoes();
        FrameMIPS32 frame = FrameMIPS32.dePrograma(programa.getBlocoPrograma());

        assertEquals(-8, frame.offset(variavel(corpo, "b")));
        assertEquals(-8, frame.offset(variavel(corpo, "c")));
        assertEquals(-12, frame.offset(variavel(corpo, "d")));
        assertEquals(-8, frame.offset(variavel(corpo, "i")));
        assertEquals(-8, frame.offset(variavel(corpo, "f")));
        assertEquals(-16, frame.offset(variavel(corpo, "h")));
        assertEquals(16, frame.getTamanho(), "o bloco mais fundo é o do enquanto, com a, f, g e h");
    }

    @Test
    void cadaFuncaoTemSeuProprioFrame() {
        Programa programa = analisa("""
                vazio f() {
                    int x;
                    novalinha;
                }
                vazio g() {
                    int y;
                    novalinha;
                }
                programa { novalinha; }
                """);
        FrameMIPS32 frameF = FrameMIPS32.deFuncao(funcao(programa, "f"));
        FrameMIPS32 frameG = FrameMIPS32.deFuncao(funcao(programa, "g"));
        Variavel x = variavel(funcao(programa, "f").getCorpo(), "x");
        Variavel y = variavel(funcao(programa, "g").getCorpo(), "y");

        assertEquals(-4, frameF.offset(x));
        assertEquals(-4, frameG.offset(y));

        BugCompilador erro = assertThrows(BugCompilador.class, () -> frameF.offset(y));
        assertTrue(erro.getMessage().contains("'y'"), erro.getMessage());
    }

    private static Programa analisa(String codigo) {
        Parser parser = new Parser(new AdaptadorLexer(new Lexer(new StringReader(codigo))));
        try {
            parser.parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Programa programa = parser.getPrograma();
        new AnalisadorSemantico().analisar(programa);
        return programa;
    }

    private static DeclaracaoFuncao funcao(Programa programa, String nome) {
        return programa.getDeclaracaoFuncoesEVariaveis().getDeclaracoesDeFuncoes().stream()
                .filter(funcao -> funcao.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("função '" + nome + "' não encontrada"));
    }

    private static Variavel global(Programa programa, String nome) {
        return programa.getDeclaracaoFuncoesEVariaveis().getDeclaracoesDeVariaveis().stream()
                .flatMap(declaracao -> declaracao.getVariaveis().stream())
                .filter(variavel -> variavel.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("variável global '" + nome + "' não encontrada"));
    }

    private static ParametroFuncao parametro(DeclaracaoFuncao funcao, String nome) {
        return funcao.getParametros().stream()
                .filter(parametro -> parametro.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("parâmetro '" + nome + "' não encontrado"));
    }

    // Declaração de uma variável local pelo nome, procurando no bloco e em todos os blocos dentro dele
    private static Variavel variavel(BlocoDeclaracoes bloco, String nome) {
        return variaveisDoBloco(bloco)
                .filter(variavel -> variavel.getNome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new AssertionError("variável '" + nome + "' não encontrada"));
    }

    private static Stream<Variavel> variaveisDoBloco(BlocoDeclaracoes bloco) {
        Stream<Variavel> proprias = bloco.getVariaveis().getDeclaracoesDeVariaveis().stream()
                .flatMap(declaracao -> declaracao.getVariaveis().stream());
        return Stream.concat(proprias, bloco.getComandos().stream().flatMap(FrameMIPS32Test::variaveisDoComando));
    }

    private static Stream<Variavel> variaveisDoComando(Comando comando) {
        return switch (comando) {
            case ComandoBloco bloco -> variaveisDoBloco(bloco.getDeclaracoes());
            case ComandoSe se -> Stream.concat(
                    variaveisDoComando(se.getConsequencia()),
                    se.getAlternativa() == null ? Stream.empty() : variaveisDoComando(se.getAlternativa())
            );
            case ComandoEnquanto enquanto -> variaveisDoComando(enquanto.getComando());
            default -> Stream.empty();
        };
    }
}
