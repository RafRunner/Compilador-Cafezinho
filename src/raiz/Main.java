package src.raiz;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        Opcoes opcoes;
        try {
            opcoes = Opcoes.ler(args);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage() + "\n");
            System.err.println("""
                    Uso: cafezinho [opções] <programa.txt>
                      -o <arquivo>    arquivo de saída (padrão: saida.asm)
                      -d, --debug     rastro do parser e pilha completa das exceções
                      --arvore        imprime a árvore sintática
                      --codigo        imprime o código original reconstruído
                    """);
            System.exit(1);
            return;
        }

        String nomeArquivoFonte = opcoes.arquivoFonte();
        File arquivoFonte = new File(nomeArquivoFonte);

        if (!arquivoFonte.exists()) {
            System.err.println("O arquivo '" + nomeArquivoFonte + "' não existe");
            System.exit(1);
        }

        boolean debugar = opcoes.debugar();

        try {
            Compilador.Resultado resultado = Compilador.compilar(new FileReader(arquivoFonte), debugar);

            if (opcoes.imprimirArvore()) {
                System.out.println("\n\n" + resultado.programa());
            }
            if (opcoes.imprimirCodigo()) {
                System.out.println("\n\n" + resultado.programa().programaOriginal());
            }

            escreveArquivoSaida(resultado.codigoObjeto(), opcoes.arquivoSaida());
        } catch (Exception e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
            System.exit(1);
        }
    }

    private static void escreveArquivoSaida(String conteudo, String arquivoSaida) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoSaida));
        writer.write(conteudo);
        writer.close();
    }
}