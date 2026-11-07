package src.raiz.sintatico;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import src.raiz.erros.ErroSintatico;
import src.raiz.generated.Lexer;
import src.raiz.generated.Parser;
import src.raiz.generated.TabelasGramatica;
import src.raiz.token.TipoToken;
import src.raiz.token.Token;
import src.raiz.token.TokensEsperados;
import src.raiz.util.RastroSintatico;

public class AdaptadorLexer implements Parser.Lexer {
    private final Lexer lexer;
    private Token ultimoToken;
    // Onde escrever cada token lido, no rastro do --debug. Null quando o rastro está desligado
    private PrintStream rastro;

    public AdaptadorLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public void setRastro(PrintStream rastro) {
        this.rastro = rastro;
    }

    @Override
    public int yylex() throws IOException {
        ultimoToken = lexer.yylex();
        if (rastro != null) {
            rastro.println(RastroSintatico.token(ultimoToken));
        }
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
                "Erro de sintaxe, inesperado " + inesperado + ", esperado: "
                        + TokensEsperados.descrever(tiposEsperados),
                ultimoToken
        );
    }
}
