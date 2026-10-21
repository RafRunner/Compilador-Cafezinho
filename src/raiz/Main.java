package src.raiz;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso incorreto! Deve ser usado: ./cafezinho <nome_do_arquivo>");
            System.exit(1);
        }

        String nomeArquivoFonte = args[0];
        File arquivoFonte = new File(nomeArquivoFonte);
        if (!arquivoFonte.exists()) {
            System.err.println("O arquivo '" + nomeArquivoFonte + "' não existe");
            System.exit(1);
        }

        boolean debugar = false;

        try {
            Compilador.Resultado resultado = Compilador.compilar(new FileReader(arquivoFonte), debugar);

            System.out.println("\n\n" + resultado.programa());
            System.out.println("\n\n" + resultado.programa().programaOriginal());

            escreveArquivoSaida(resultado.codigoObjeto());
        } catch (Exception e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
            System.exit(1);
        }
    }

    private static void escreveArquivoSaida(String conteudo) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./saida.asm"));
        writer.write(conteudo);
        writer.close();
    }
}