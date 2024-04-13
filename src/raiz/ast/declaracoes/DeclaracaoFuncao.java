package src.raiz.ast.declaracoes;

import src.raiz.ast.ParametroFuncao;
import src.raiz.ast.TipoVariavelNo;
import src.raiz.ast.comandos.ComandoBloco;
import src.raiz.ast.comandos.ComandoRetorno;
import src.raiz.ast.comandos.ComandoSe;
import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.Collections;
import java.util.List;

// Declaração de uma função. Contem tudo necessário para chamá-la e executá-la
public class DeclaracaoFuncao extends Declaracao {

    private final TipoVariavelNo tipoRetorno;
    private final String nome;
    private final List<ParametroFuncao> parametros;
    private final BlocoDeclaracoes corpo;

    public DeclaracaoFuncao(
            Token token,
            TipoVariavelNo tipoRetorno,
            BlocoDeclaracoes corpo,
            List<ParametroFuncao> parametros
    ) throws ErroSemantico {
        super(token);
        this.tipoRetorno = tipoRetorno;
        this.nome = token.lexema();
        this.parametros = Collections.unmodifiableList(parametros);
        this.corpo = corpo;

        boolean temRetornoFinal = verificaRetornoBloco(corpo);

        // Verifica se a função termina com um retorno válido
        if (tipoRetorno.isTipoVazio()) {
            if (temRetornoFinal) {
                throw new ErroSemantico("Função '" + nome + "' é de retorno vazio, logo não deve terminar com expressão de retorno", token);
            }
        } else if (!temRetornoFinal) {
            throw new ErroSemantico("Função '" + nome + "' deve terminar com expressão de retorno", token);
        }
    }

    private boolean verificaRetornoBloco(BlocoDeclaracoes bloco) {
        // Função / bloco vazio
        if (bloco.getDeclaracoes().isEmpty()) {
            return false;
        }

        Declaracao ultimaDeclaracao = bloco.getDeclaracoes().getLast();
        return validaRetorno(ultimaDeclaracao);
    }

    private boolean validaRetorno(Declaracao declaracao) {
        return switch (declaracao) {
            // Caso base: declaração de retorno. Se for um retorno vazio é tratado posteriormente
            case ComandoRetorno ignored -> true;

            // Recursão para blocos de declarações
            case BlocoDeclaracoes blocoDeclaracoes -> verificaRetornoBloco(blocoDeclaracoes);

            // Recursão para comandos de bloco
            case ComandoBloco comandoBloco -> verificaRetornoBloco(comandoBloco.getDeclaracoes());

            // Recursão para comandos 'se', incluindo verificações para 'se' e 'senão'
            case ComandoSe comandoSe -> {
                // Se não tem bloco 'senão', é inválido
                if (comandoSe.getAlternativa() == null) {
                    yield false;
                }

                // Os casos 'se' e 'senão' devem terminar com retorno
                yield validaRetorno(comandoSe.getConsequencia()) && validaRetorno(comandoSe.getAlternativa());
            }

            // Não é um retorno válido
            default -> false;
        };
    }

    public TipoVariavelNo getTipoRetorno() {
        return tipoRetorno;
    }

    public String getNome() {
        return nome;
    }

    public List<ParametroFuncao> getParametros() {
        return parametros;
    }

    public BlocoDeclaracoes getCorpo() {
        return corpo;
    }

    @Override
    public String codigoOriginal() {
        String paramString = AstUtil.codigosOriginais(this.parametros, ", ");

        return tipoRetorno.codigoOriginal() + " " + getToken().lexema() + "(" + paramString + ") "
                + corpo.codigoOriginal();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "DeclaracaoFuncao {\n"
                + getIndentacao(profundidade) + "nome: '" + nome + "',\n"
                + getIndentacao(profundidade) + "tipoRetorno: " + tipoRetorno + ",\n"
                + getIndentacao(profundidade) + "parametros: [" + AstUtil.toStrings(parametros, ", ") + "],\n"
                + getIndentacao(profundidade) + "corpo: " + corpo.representacaoArvore(profundidade + 1) + "\n"
                + getIndentacao(profundidade - 1) + "}";
    }
}
