package src.raiz.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import src.raiz.token.TipoToken;
import src.raiz.token.Token;

// Rastro da análise sintática (opção --debug): os tokens lidos e as regras reduzidas, na notação do docs/regras.txt.
// Serve para depurar a gramática num exemplo pequeno; num programa grande, a árvore do --arvore diz mais
//
// Os tokens vêm do AdaptadorLexer. As reduções vêm do rastro do Bison (parse.trace), que só existe como texto:
//
//   Reducing stack by rule 13 (line 205):
//      $1 = nterm Tipo (...)
//      $2 = token IDENTIFICADOR (...)
//   -> $$ = nterm ListaParametrosCont (...)
//
// O filtrando() lê esse texto e escreve só "ListaParametrosCont → Tipo IDENTIFICADOR". Os valores entre parênteses
// são o toString() de cada objeto e ficam de fora: o Bison Java não tem como formatá-los (o %printer é só de C e C++)
public class RastroSintatico {

    private static final int LARGURA_COLUNA = 50;

    public static String token(Token token) {
        String descricao = token.tipo().getDescricao();
        // Para identificadores e literais, o nome da classe de token não diz qual foi escrito
        if (descricao.equals(token.tipo().name())) {
            descricao += " '" + token.lexema() + "'";
        }
        return String.format("%-" + LARGURA_COLUNA + "s  %s", "token " + descricao, token.descricaoLocal());
    }

    // Um PrintStream para o setDebugStream do parser, que escreve no destino só as reduções
    public static PrintStream filtrando(PrintStream destino) {
        return new PrintStream(new Filtro(destino), true, StandardCharsets.UTF_8);
    }

    // Retorna null para as regras que só repassam outro não terminal (UnExpr → PrimExpr): são a maior parte das
    // reduções e não acrescentam nada
    static String reducao(String ladoEsquerdo, List<String> ladoDireito) {
        if (ladoDireito.size() == 1 && !isTerminal(ladoDireito.getFirst())) {
            return null;
        }
        String producao = ladoDireito.isEmpty()
                ? "ε"
                : ladoDireito.stream().map(RastroSintatico::simbolo).collect(Collectors.joining(" "));
        return ladoEsquerdo + " → " + producao;
    }

    private static String simbolo(String nome) {
        return isTerminal(nome) ? TipoToken.valueOf(nome).getDescricao() : nome;
    }

    private static boolean isTerminal(String nome) {
        for (TipoToken tipo : TipoToken.values()) {
            if (tipo.name().equals(nome)) {
                return true;
            }
        }
        return false;
    }

    // Junta os bytes que o Bison escreve até cada quebra de linha e interpreta a linha inteira
    private static final class Filtro extends OutputStream {

        private final PrintStream destino;
        private final ByteArrayOutputStream linha = new ByteArrayOutputStream();
        private final List<String> ladoDireito = new ArrayList<>();

        Filtro(PrintStream destino) {
            this.destino = destino;
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                processa(linha.toString(StandardCharsets.UTF_8));
                linha.reset();
            } else {
                linha.write(b);
            }
        }

        // Só três tipos de linha importam. O resto (estados, pilha, LAC, e as linhas seguintes de valores que
        // ocupam várias linhas, como os nós da árvore) é descartado
        private void processa(String linha) {
            if (linha.startsWith("Reducing stack")) {
                ladoDireito.clear();
            } else if (linha.startsWith("   $")) {
                // " $2 = token IDENTIFICADOR (...)": o símbolo é a quarta palavra
                ladoDireito.add(palavra(linha, 3));
            } else if (linha.startsWith("-> $$ = nterm ")) {
                String reducao = reducao(palavra(linha, 4), ladoDireito);
                if (reducao != null) {
                    destino.println(reducao);
                }
            }
        }

        private static String palavra(String linha, int indice) {
            return linha.trim().split("\\s+")[indice];
        }
    }
}
