package src.raiz;

import src.raiz.ast.Programa;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.VisitadorDeNos;
import src.raiz.compilador.VisitadorDeNosMIPS32;
import src.raiz.generated.Parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso incorreto! Dese ser usado: ./cafezinho <nome_do_arquivo>");
            return;
        }

        String nomeArquivoFonte = args[0];
        File arquivoFonte = new File(nomeArquivoFonte);
        if (!arquivoFonte.exists()) {
            System.err.println("O arquivo '" + nomeArquivoFonte + "' não existe");
            return;
        }

        boolean debugar = true;

        try {
            Parser parser = new Parser();
            parser.analisar(new FileReader(arquivoFonte), debugar);

            Programa programa = parser.getPrograma();
            GeradorDeCodigo geradorDeCodigo = new GeradorDeCodigo();
            VisitadorDeNos visitador = new VisitadorDeNosMIPS32(programa, geradorDeCodigo);
            visitador.visitarPorgrama();

            System.out.println("\n\n" + programa);
            System.out.println(programa.programaOriginal());
            System.out.println("\n\n" + geradorDeCodigo.codigoObjeto());
        } catch (RuntimeException e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
        }
    }
}