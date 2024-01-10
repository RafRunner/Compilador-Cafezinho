package src.raiz;

import java.io.*;

import src.raiz.ast.Declaracao;
import src.raiz.ast.DeclaracaoFuncoesEVariaveis;
import src.raiz.ast.Programa;
import src.raiz.compilador.GeradorDeCodigo;
import src.raiz.compilador.VisitadorDeNos;
import src.raiz.compilador.VisitadorDeNosMIPS32;
import src.raiz.compilador.tabeladesimbolos.TabelaDeSimbolos;
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
        TabelaDeSimbolos tabelaDeSimbolos = new TabelaDeSimbolos();
        GeradorDeCodigo geradorDeCodigo = new GeradorDeCodigo();
        VisitadorDeNos visitador = new VisitadorDeNosMIPS32(tabelaDeSimbolos, geradorDeCodigo);

        try {
            Parser parser = new Parser();
            parser.analisar(new FileReader(arquivoFonte), debugar);

            Programa programa = parser.getPrograma();

            for (Declaracao declaracao : programa.getDeclaracoes()) {
                if (declaracao instanceof DeclaracaoFuncoesEVariaveis) {
                    visitador.visitarDeclaracaoFuncaoEVariaveis((DeclaracaoFuncoesEVariaveis) declaracao);
                }
            }

            System.out.println("\n\n" + programa);
            System.out.println(programa.programaOriginal());
            System.out.println(geradorDeCodigo.codigoObjeto());
        } catch (RuntimeException e) {
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
        }
    }
}