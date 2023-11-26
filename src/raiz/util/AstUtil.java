package src.raiz.util;

import java.util.List;

import src.raiz.ast.NoSintatico;

public class AstUtil {

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

}
