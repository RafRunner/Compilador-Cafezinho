package src.raiz;

import src.raiz.ast.Programa;
import src.raiz.compilador.Backend;
import src.raiz.compilador.mips32.BackendMIPS32;
import src.raiz.generated.Lexer;
import src.raiz.generated.Parser;
import src.raiz.semantica.AnalisadorSemantico;
import src.raiz.sintatico.AdaptadorLexer;
import src.raiz.util.RastroSintatico;

import java.io.IOException;
import java.io.Reader;

// Ponto de entrada do compilador: transforma o código fonte cafezinho em assembly MIPS32
public class Compilador {

    public record Resultado(Programa programa, String codigoObjeto) {
    }

    public static Resultado compilar(Reader fonte, boolean debugar) throws IOException {
        // Análise léxica e sintática: monta a AST
        AdaptadorLexer lexer = new AdaptadorLexer(new Lexer(fonte));
        Parser parser = new Parser(lexer);
        if (debugar) {
            // Os dois escrevem no mesmo System.out, na ordem em que acontecem: cada token e as reduções que ele causa
            lexer.setRastro(System.out);
            parser.setDebugLevel(1);
            parser.setDebugStream(RastroSintatico.filtrando(System.out));
        }
        parser.parse();
        Programa programa = parser.getPrograma();

        // Análise semântica: valida as regras da linguagem e anota a AST com tipos e símbolos
        new AnalisadorSemantico().analisar(programa);

        // Geração de código: traduz a AST anotada para a arquitetura alvo
        Backend backend = new BackendMIPS32();
        return new Resultado(programa, backend.gerarCodigo(programa));
    }

    public static Resultado compilar(Reader fonte) throws IOException {
        return compilar(fonte, false);
    }
}
