package src.raiz.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import src.raiz.Compilador;

// O filtro do rastro lê o texto que o Bison escreve com parse.trace. Se uma versão nova do Bison mudar esse texto,
// o filtro não quebra: só para de mostrar as reduções. Este teste não confere a saída inteira, que muda a cada
// alteração na gramática, só que tokens e reduções continuam aparecendo
class RastroSintaticoTest {

    @Test
    void debugMostraTokensEReducoes() throws IOException {
        String rastro = rastroDe("programa { }");

        assertTrue(rastro.contains("token 'programa'"), "sem os tokens lidos:\n" + rastro);
        // A regra inicial é a mais estável da gramática
        assertTrue(rastro.contains("Programa → DeclFuncVar DeclProg"), "sem as reduções:\n" + rastro);
    }

    // O rastro vai para o System.out, como no --debug
    private static String rastroDe(String codigo) throws IOException {
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
            Compilador.compilar(new StringReader(codigo), true);
        } finally {
            System.setOut(saidaOriginal);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }
}
