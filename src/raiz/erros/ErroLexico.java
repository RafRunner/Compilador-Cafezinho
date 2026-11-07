package src.raiz.erros;

// Texto que não forma nenhum token válido: caractere desconhecido, string ou comentário sem fechar
public class ErroLexico extends ErroCompilacao {

    public ErroLexico(String mensagem, int linha, int coluna, String proximoDe) {
        super("Erro léxico: " + mensagem, linha, coluna, proximoDe);
    }
}
