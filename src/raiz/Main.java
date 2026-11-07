package src.raiz;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import src.raiz.erros.ErroCompilacao;

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

        Path arquivoFonte = Path.of(opcoes.arquivoFonte());
        if (!Files.exists(arquivoFonte)) {
            System.err.println("O arquivo '" + arquivoFonte + "' não existe");
            System.exit(1);
        }

        boolean debugar = opcoes.debugar();

        Compilador.Resultado resultado;
        try (Reader fonte = Files.newBufferedReader(arquivoFonte, StandardCharsets.UTF_8)) {
            resultado = Compilador.compilar(fonte, debugar);
        } catch (ErroCompilacao e) {
            // Erro no programa sendo compilado: a mensagem já diz o que e onde, a pilha do Java só atrapalharia
            if (debugar) {
                e.printStackTrace(System.err);
            } else {
                System.err.println(e.getMessage());
            }
            System.exit(1);
            return;
        } catch (IOException e) {
            System.err.println("Não foi possível ler '" + arquivoFonte + "': " + e.getMessage());
            System.exit(1);
            return;
        }
        // Qualquer outra exceção é bug do compilador e sobe com a pilha completa

        if (opcoes.imprimirArvore()) {
            System.out.println("\n\n" + resultado.programa());
        }
        if (opcoes.imprimirCodigo()) {
            System.out.println("\n\n" + resultado.programa().programaOriginal());
        }

        try {
            Files.writeString(Path.of(opcoes.arquivoSaida()), resultado.codigoObjeto(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Não foi possível escrever '" + opcoes.arquivoSaida() + "': " + e.getMessage());
            System.exit(1);
        }
    }
}

