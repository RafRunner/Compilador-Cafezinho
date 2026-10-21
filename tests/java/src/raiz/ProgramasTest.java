package src.raiz;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// Testes de ponta a ponta: cada programa em tests/programas é compilado, executado no simulador MARS
// e sua saída comparada com o arquivo esperado. Para um programa <nome>.txt:
//   <nome>.saida       saída esperada (espaços e quebras de linha no final são ignorados)
//   <nome>.saida.regex alternativa a .saida, para programas com saída não determinística
//   <nome>.entrada     (opcional) entrada padrão do programa
//   <nome>.status      (opcional) código de saída esperado, padrão 0
// Cada programa em tests/erros deve falhar na compilação com uma mensagem que contém <nome>.erro
class ProgramasTest {

    private static final Path DIRETORIO_TESTES = Path.of(System.getProperty("cafezinho.testes", "tests"));
    private static final String MARS = System.getProperty("cafezinho.mars");
    private static final int LIMITE_PASSOS = 50_000_000;
    private static final int CODIGO_ERRO_MONTAGEM = 100;
    private static final int CODIGO_ERRO_EXECUCAO = 101;

    @TestFactory
    Stream<DynamicTest> programasExecutamComSaidaEsperada() throws IOException {
        return fontes(DIRETORIO_TESTES.resolve("programas"))
                .map(fonte -> DynamicTest.dynamicTest(nome(fonte), () -> executaPrograma(fonte)));
    }

    @TestFactory
    Stream<DynamicTest> programasInvalidosSaoRejeitados() throws IOException {
        return fontes(DIRETORIO_TESTES.resolve("erros"))
                .map(fonte -> DynamicTest.dynamicTest(nome(fonte), () -> verificaErro(fonte)));
    }

    private void executaPrograma(Path fonte) throws Exception {
        String codigo = Compilador.compilar(new StringReader(Files.readString(fonte))).codigoObjeto();

        Path asm = Files.createTempFile(nome(fonte), ".asm");
        Files.writeString(asm, codigo);

        Path entrada = irmao(fonte, ".entrada");
        Execucao execucao = executaNoMars(asm, Files.exists(entrada) ? entrada : null);

        assertEquals("", execucao.erros(), "MARS reportou erros:\n" + execucao.erros());

        Path status = irmao(fonte, ".status");
        int statusEsperado = Files.exists(status) ? Integer.parseInt(Files.readString(status).strip()) : 0;
        assertEquals(statusEsperado, execucao.status(), "código de saída");

        Path saidaRegex = irmao(fonte, ".saida.regex");
        if (Files.exists(saidaRegex)) {
            String padrao = Files.readString(saidaRegex).strip();
            assertTrue(Pattern.compile(padrao, Pattern.DOTALL).matcher(execucao.saida()).matches(),
                    "saída '" + execucao.saida() + "' não corresponde a /" + padrao + "/");
        } else {
            assertEquals(Files.readString(irmao(fonte, ".saida")).stripTrailing(), execucao.saida());
        }

        Files.delete(asm);
    }

    private void verificaErro(Path fonte) throws IOException {
        String esperado = Files.readString(irmao(fonte, ".erro")).strip();
        String codigo = Files.readString(fonte);

        Exception erro = assertThrows(Exception.class, () -> Compilador.compilar(new StringReader(codigo)));
        assertTrue(erro.getMessage() != null && erro.getMessage().contains(esperado),
                "mensagem '" + erro.getMessage() + "' não contém '" + esperado + "'");
    }

    private record Execucao(String saida, String erros, int status) {
    }

    private Execucao executaNoMars(Path asm, Path entrada) throws Exception {
        assertNotNull(MARS, "propriedade cafezinho.mars não definida, rode os testes pelo Gradle");

        ProcessBuilder builder = new ProcessBuilder(
                "java", "-Dstdout.encoding=UTF-8", "-jar", MARS, "nc", "me",
                "ae" + CODIGO_ERRO_MONTAGEM, "se" + CODIGO_ERRO_EXECUCAO,
                String.valueOf(LIMITE_PASSOS), asm.toString()
        );
        Path saida = Files.createTempFile("saida", ".txt");
        Path erros = Files.createTempFile("erros", ".txt");
        builder.redirectOutput(saida.toFile());
        builder.redirectError(erros.toFile());
        if (entrada != null) {
            builder.redirectInput(entrada.toFile());
        }

        Process processo = builder.start();
        if (!processo.waitFor(60, TimeUnit.SECONDS)) {
            processo.destroyForcibly();
            fail("MARS não terminou em 60 segundos");
        }

        Execucao execucao = new Execucao(
                Files.readString(saida, StandardCharsets.UTF_8).stripTrailing(),
                limpaErros(Files.readString(erros)),
                processo.exitValue()
        );
        Files.delete(saida);
        Files.delete(erros);

        return execucao;
    }

    // A JVM pode escrever avisos próprios no stderr que não são erros do programa
    private String limpaErros(String erros) {
        return erros.lines()
                .filter(linha -> !linha.startsWith("Picked up JAVA_TOOL_OPTIONS"))
                .filter(linha -> !linha.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private static Stream<Path> fontes(Path diretorio) throws IOException {
        try (Stream<Path> arquivos = Files.list(diretorio)) {
            List<Path> fontes = arquivos.filter(p -> p.toString().endsWith(".txt")).sorted().toList();
            return fontes.stream();
        }
    }

    private static String nome(Path fonte) {
        String arquivo = fonte.getFileName().toString();
        return arquivo.substring(0, arquivo.length() - ".txt".length());
    }

    private static Path irmao(Path fonte, String extensao) {
        return fonte.resolveSibling(nome(fonte) + extensao);
    }
}
