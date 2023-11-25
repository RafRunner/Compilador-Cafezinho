package src.raiz;

import java.io.*;

import src.raiz.ast.Programa;
import src.raiz.generated.Parser;

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

            System.out.println("\n\n" + programa);
            System.out.println(programa.programaOriginal());
        } catch (RuntimeException e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
        }
    }
}