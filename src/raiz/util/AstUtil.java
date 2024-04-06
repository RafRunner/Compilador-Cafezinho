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

    public static String montaMensagemErro(String mensagemErro, Token t) {
        if (t != null) {
            return "ERRO: " + mensagemErro + "; linha: " + (t.getLinha() + 1) + ", coluna: " + (t.getColuna() + 1)
                   + ", próximo de " + t.getLexema();
        } else {
            return "ERRO: " + mensagemErro;
        }
    }

}
