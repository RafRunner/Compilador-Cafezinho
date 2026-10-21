package src.raiz;

import src.raiz.ast.Programa;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.VisitadorDeNos;
import src.raiz.compilador.mips32.GeradorDeCodigoMIPS32;
import src.raiz.compilador.mips32.VisitadorDeNosMIPS32;
import src.raiz.generated.Parser;

import java.io.Reader;

// Ponto de entrada do compilador: transforma o código fonte cafezinho em assembly MIPS32
public class Compilador {

    public record Resultado(Programa programa, String codigoObjeto) {
    }

    public static Resultado compilar(Reader fonte, boolean debugar) {
        Parser parser = new Parser();
        Programa programa = parser.analisar(fonte, debugar);

        GeradorDeCodigo geradorDeCodigo = new GeradorDeCodigoMIPS32();
        VisitadorDeNos visitador = new VisitadorDeNosMIPS32(programa, geradorDeCodigo);
        visitador.visitarPorgrama();

        return new Resultado(programa, geradorDeCodigo.geraCodigoObjeto());
    }

    public static Resultado compilar(Reader fonte) {
        return compilar(fonte, false);
    }
}
