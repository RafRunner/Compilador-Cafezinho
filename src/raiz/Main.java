package src.raiz;

import java.io.*;

import src.raiz.generated.Parser;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso incorreto! Dese ser usado: ./cafezinho teste.z");
            return;
        }

        String nomeArquivoFonte = args[0];
        File arquivoFonte = new File(nomeArquivoFonte);
        if (!arquivoFonte.exists()) {
            System.err.println("O arquivo '" + nomeArquivoFonte + "' não existe");
            return;
        }

        if (!nomeArquivoFonte.endsWith(".z")) {
            System.err
                    .println("O arquivo '" + nomeArquivoFonte + "' não é da linguagem cafezinho. Deve termianr com .z");
            return;
        }

        Parser parser = new Parser();
        parser.analisar(new FileReader(arquivoFonte), true);
    }
}