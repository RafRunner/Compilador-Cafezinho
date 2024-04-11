package src.raiz.util;

import java.util.List;
import java.util.stream.Collectors;

import src.raiz.ast.NoSintatico;
import src.raiz.token.Token;

public class AstUtil {

    // Junta vários Nós em uma string dos codigos originais
    public static String codigosOriginais(List<? extends NoSintatico> nos, String separador) {
        return nos.stream().map(NoSintatico::codigoOriginal).collect(Collectors.joining(separador));
    }

    // Junta vários Nós em uma string dos toString para debugg
    public static String toStrings(List<? extends NoSintatico> nos, String separador) {
        return nos.stream().map(Object::toString).collect(Collectors.joining(separador));
    }

    public static String representacoesArvore(List<? extends NoSintatico> nos, int profundidade) {
        if (nos.isEmpty()) {
            return "[]";
        }

        return "[\n" + getIdentacao(profundidade + 1)
               + nos.stream().map(no -> no.representacaoArvore(profundidade + 2))
                       .collect(Collectors.joining(",\n" + getIdentacao(profundidade + 1))) + "\n"
               + getIdentacao(profundidade) + "]";
    }

    public static String getIdentacao(int profundidade) {
        return "  ".repeat(Math.max(0, profundidade));
    }

    public static String montaMensagemErro(String mensagemErro, Token t) {
        if (t != null) {
            return "ERRO: " + mensagemErro + "; linha: " + (t.linha() + 1) + ", coluna: " + (t.coluna() + 1)
                   + ", próximo de " + t.lexema();
        } else {
            return "ERRO: " + mensagemErro;
        }
    }

}
