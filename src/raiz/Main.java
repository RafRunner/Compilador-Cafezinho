package src.raiz;

import java.io.*;

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

        try {
            Parser parser = new Parser();
            parser.analisar(new FileReader(arquivoFonte), true);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}