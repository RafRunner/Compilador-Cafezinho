package src.raiz;

import src.raiz.ast.Programa;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.mips32.GeradorDeCodigoMIPS32;
import src.raiz.compilador.VisitadorDeNos;
import src.raiz.compilador.mips32.VisitadorDeNosMIPS32;
import src.raiz.generated.Parser;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Uso incorreto! Deve ser usado: ./cafezinho <nome_do_arquivo>");
            return;
        }

        String nomeArquivoFonte = args[0];
        File arquivoFonte = new File(nomeArquivoFonte);
        if (!arquivoFonte.exists()) {
            System.err.println("O arquivo '" + nomeArquivoFonte + "' não existe");
            return;
        }

        boolean debugar = false;

        try {
            Parser parser = new Parser();
            Programa programa = parser.analisar(new FileReader(arquivoFonte), debugar);

            GeradorDeCodigo geradorDeCodigo = new GeradorDeCodigoMIPS32();
            VisitadorDeNos visitador = new VisitadorDeNosMIPS32(programa, geradorDeCodigo);
            visitador.visitarPorgrama();

            String codigoObjeto = geradorDeCodigo.geraCodigoObjeto();

            System.out.println("\n\n" + programa);
            System.out.println("\n\n" + programa.programaOriginal());

            escreveArquivoSaida(codigoObjeto);
        } catch (Exception e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
        }
    }

    private static void escreveArquivoSaida(String conteudo) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./saida.asm"));
        writer.write(conteudo);
        writer.close();
    }
}