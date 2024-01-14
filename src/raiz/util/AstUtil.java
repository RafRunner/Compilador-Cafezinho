package src.raiz.util;

import java.util.List;

import src.raiz.ast.NoSintatico;
import src.raiz.token.Token;

public class AstUtil {

    // Junta vários Nós em uma string de representações
    public static void representacoesString(
            StringBuilder sb,
            List<? extends NoSintatico> nos,
            String separador) {
        int i = 0;
        for (NoSintatico no : nos) {
            sb.append(no.representacaoString());
            if (i < nos.size() - 1) {
                sb.append(separador);
            }
            i++;
        }
    }

    // Junta vários Nós em uma string dos toString para debugg
    public static void toStrings(
            StringBuilder sb,
            List<? extends NoSintatico> nos,
            String separador) {
        int i = 0;
        for (NoSintatico no : nos) {
            sb.append(no);
            if (i < nos.size() - 1) {
                sb.append(separador);
            }
            i++;
        }
    }

    public static String montaMensagemErro(String mensagemErro, Token t) {
        if (t != null) {
            return "ERRO: " + mensagemErro + "; linha: " + (t.getLinha() + 1) + ", coluna: " + (t.getColuna() + 1) + ", próximo de " + t.getLexema();
        } else {
            return "ERRO: " + mensagemErro;
        }
    }

}
