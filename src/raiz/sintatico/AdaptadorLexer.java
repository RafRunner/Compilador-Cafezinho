package src.raiz.sintatico;

import java.io.IOException;
import java.util.Set;

import src.raiz.erros.ErroSintatico;
import src.raiz.generated.Lexer;
import src.raiz.generated.Parser;
import src.raiz.generated.TabelasGramatica;
import src.raiz.token.TipoToken;
import src.raiz.token.Token;
import src.raiz.token.TokensEsperados;

public class AdaptadorLexer implements Parser.Lexer {
    private final Lexer lexer;
    private Token ultimoToken;

    public AdaptadorLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public int yylex() throws IOException {
        ultimoToken = lexer.yylex();
        return TabelasGramatica.codigoToken(ultimoToken.tipo());
    }

    @Override
    public Object getLVal() {
        return ultimoToken;
    }

    @Override
    public void yyerror(String mensagem) {
        // Com parse.error custom, os erros de sintaxe vão para o reportSyntaxError. Aqui só chegam os outros erros do
        // parser, como a pilha estourar
        throw new ErroSintatico(mensagem, ultimoToken);
    }

    @Override
    public void reportSyntaxError(Parser.Context ctx) {
        Set<TipoToken> tiposEsperados = Parser.tokensEsperados(ctx);

        String inesperado = ultimoToken.tipo() == TipoToken.EOF
                ? "fim do arquivo"
                : ultimoToken.tipo().getDescricao();
        throw new ErroSintatico(
                "Erro de sintaxe, inesperado " + inesperado + ", esperado: " + TokensEsperados.descrever(tiposEsperados),
                ultimoToken
        );
    }
}
