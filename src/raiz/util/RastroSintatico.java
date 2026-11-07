package src.raiz.util;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import src.raiz.ast.NoSintatico;
import src.raiz.token.TipoToken;
import src.raiz.token.Token;

// Formata o rastro da análise sintática (opção --debug): os tokens lidos e as regras reduzidas, na notação do
// docs/regras.txt, cada uma com o trecho de código que reconheceu
public class RastroSintatico {

    private static final int LARGURA_COLUNA = 50;
    private static final int TAMANHO_MAXIMO_TRECHO = 60;

    public static String token(Token token) {
        String descricao = token.tipo().getDescricao();
        // Para identificadores e literais, o nome da classe de token não diz qual foi escrito
        if (descricao.equals(token.tipo().name())) {
            descricao += " " + token.lexema();
        }
        return alinha("token " + descricao, token.descricaoLocal());
    }

    // A regra vem do BYACC/J no formato "MulExpr : MulExpr VEZES ConvExpr". Retorna null para as regras que só
    // repassam outro não terminal (UnExpr → PrimExpr): são a maior parte das reduções e não acrescentam nada
    public static String reducao(String regra, Object valor) {
        int separador = regra.indexOf(" :");
        String ladoEsquerdo = regra.substring(0, separador);
        List<String> ladoDireito = Arrays.stream(regra.substring(separador + 2).trim().split(" "))
                .filter(simbolo -> !simbolo.isEmpty())
                .toList();

        if (ladoDireito.size() == 1 && !isTerminal(ladoDireito.getFirst())) {
            return null;
        }

        String producao = ladoDireito.isEmpty()
                ? "ε"
                : ladoDireito.stream().map(RastroSintatico::simbolo).collect(Collectors.joining(" "));
        return alinha(ladoEsquerdo + " → " + producao, trecho(valor));
    }

    public static PrintStream filtrando(PrintStream printStream) {
        return printStream;
    }

    private static String simbolo(String nome) {
        return isTerminal(nome) ? TipoToken.valueOf(nome).getDescricao() : nome;
    }

    private static boolean isTerminal(String nome) {
        return Arrays.stream(TipoToken.values()).anyMatch(tipo -> tipo.name().equals(nome));
    }

    // O código que a regra reconheceu, a partir do valor que a ação montou: um nó, um token ou uma lista deles
    private static String trecho(Object valor) {
        String codigo = codigo(valor).replaceAll("\\s+", " ").trim();
        if (codigo.length() > TAMANHO_MAXIMO_TRECHO) {
            codigo = codigo.substring(0, TAMANHO_MAXIMO_TRECHO - 3) + "...";
        }
        return codigo;
    }

    private static String codigo(Object valor) {
        return switch (valor) {
            case null -> "";
            case NoSintatico no -> codigoDoNo(no);
            case Token token -> token.lexema();
            case Collection<?> colecao ->
                colecao.stream().map(RastroSintatico::codigo).collect(Collectors.joining(", "));
            case Object[] partes -> Arrays.stream(partes).map(RastroSintatico::codigo).collect(Collectors.joining(" "));
            default -> valor.toString();
        };
    }

    // Durante a análise alguns nós ainda estão incompletos (em 'int a, b;' as variáveis só recebem o tipo quando a
    // declaração inteira é reduzida), e o rastro não pode derrubar o compilador por isso
    private static String codigoDoNo(NoSintatico no) {
        try {
            return no.codigoOriginal();
        } catch (RuntimeException e) {
            return "(" + no.getClass().getSimpleName() + " incompleto)";
        }
    }

    private static String alinha(String esquerda, String direita) {
        return String.format("%-" + LARGURA_COLUNA + "s  %s", esquerda, direita).stripTrailing();
    }
}
