package src.raiz.util;

import java.util.List;
import java.util.stream.Collectors;

import src.raiz.ast.NoSintatico;
import src.raiz.token.Token;

public class AstUtil {

    // Junta vários Nós em uma string dos códigos originais
    public static String codigosOriginais(List<? extends NoSintatico> nos, String separador) {
        return nos.stream().map(NoSintatico::codigoOriginal).collect(Collectors.joining(separador));
    }

    // Junta vários Nós em uma string dos toString para debug
    public static String toStrings(List<? extends NoSintatico> nos, String separador) {
        return nos.stream().map(Object::toString).collect(Collectors.joining(separador));
    }

    public static String representacoesArvore(List<? extends NoSintatico> nos, int profundidade) {
        if (nos.isEmpty()) {
            return "[]";
        }

        String nosString = nos.stream()
                .map(no -> no.representacaoArvore(profundidade + 1))
                .collect(Collectors.joining(",\n" + getIndentacao(profundidade)));

        return "[\n"
                + getIndentacao(profundidade) + nosString + "\n"
                + getIndentacao(profundidade - 1) + "]";
    }

    public static String getIndentacao(int profundidade) {
        return "  ".repeat(Math.max(0, profundidade));
    }

    // Indenta em um nível cada linha não vazia do código. Como cada bloco indenta o seu próprio conteúdo,
    // blocos aninhados acumulam os níveis sem precisar saber a profundidade
    public static String indenta(String codigo) {
        return codigo.lines()
                .map(linha -> linha.isEmpty() ? linha : getIndentacao(1) + linha)
                .collect(Collectors.joining("\n"));
    }

    public static String montaMensagemErro(String mensagemErro, Token t) {
        if (t == null) {
            return "ERRO: " + mensagemErro;
        }
        return montaMensagemErro(mensagemErro, t.linha(), t.coluna(), t.lexema());
    }

    // Linha e coluna começam em 0 no lexer, mas são mostradas a partir de 1, como nos editores. O lexema fica de
    // fora quando é vazio, como no fim do arquivo
    public static String montaMensagemErro(String mensagemErro, int linha, int coluna, String proximoDe) {
        String mensagem = "ERRO: " + mensagemErro + "; linha: " + (linha + 1) + ", coluna: " + (coluna + 1);
        if (proximoDe != null && !proximoDe.isEmpty()) {
            mensagem += ", próximo de " + proximoDe;
        }
        return mensagem;
    }

}
