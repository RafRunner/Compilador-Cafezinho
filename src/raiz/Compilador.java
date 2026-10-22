package src.raiz;

import src.raiz.ast.Programa;
import src.raiz.compilador.Backend;
import src.raiz.compilador.mips32.BackendMIPS32;
import src.raiz.generated.Parser;
import src.raiz.semantica.AnalisadorSemantico;

import java.io.Reader;

// Ponto de entrada do compilador: transforma o código fonte cafezinho em assembly MIPS32
public class Compilador {

    public record Resultado(Programa programa, String codigoObjeto) {
    }

    public static Resultado compilar(Reader fonte, boolean debugar) {
        // Análise léxica e sintática: monta a AST
        Programa programa = new Parser().analisar(fonte, debugar);

        // Análise semântica: valida as regras da linguagem e anota a AST com tipos e símbolos
        new AnalisadorSemantico().analisar(programa);

        // Geração de código: traduz a AST anotada para a arquitetura alvo
        Backend backend = new BackendMIPS32();
        return new Resultado(programa, backend.gerarCodigo(programa));
    }

    public static Resultado compilar(Reader fonte) {
        return compilar(fonte, false);
    }
}
