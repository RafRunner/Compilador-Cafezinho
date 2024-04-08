package src.raiz.ast;

import src.raiz.ast.comandos.ComandoBloco;
import src.raiz.ast.comandos.ComandoRetorno;
import src.raiz.ast.comandos.ComandoSe;
import src.raiz.erros.ErroSemantico;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

import java.util.List;

// Declaração de uma função. Contem tudo necessário para chamá-la e executá-la
public class DeclaracaoFuncao extends Declaracao {

    private final TipoVariavelNo tipoRetorno;
    private final String nome;
    private final List<ParametroFuncao> parametros;
    private final BlocoDeclaracoes corpo;

    public DeclaracaoFuncao(Token token, TipoVariavelNo tipoRetorno, BlocoDeclaracoes corpo,
            List<ParametroFuncao> parametros) throws ErroSemantico {
        super(token);
        this.tipoRetorno = tipoRetorno;
        this.nome = token.lexema();
        this.parametros = parametros;
        this.corpo = corpo;

        // Verifica se a função termina com um retorno válido
        if (!verificaRetornoBloco(corpo)) {
            throw new ErroSemantico("Função " + nome + " deve terminar com expressão de retorno", token);
        }
    }

    private boolean verificaRetornoBloco(BlocoDeclaracoes bloco) {
        // Função / bloco vazio
        if (bloco.getDeclaracoes().isEmpty()) {
            return false;
        }

        Declaracao ultimaDeclaracao = bloco.getDeclaracoes().get(bloco.getDeclaracoes().size() - 1);
        return validaRetorno(ultimaDeclaracao);
    }

    private boolean validaRetorno(Declaracao declaracao) {
        // Caso base: declaração de retorno
        if (declaracao instanceof ComandoRetorno) {
            return true;
        }

        // Recursão para blocos de declarações
        if (declaracao instanceof BlocoDeclaracoes blocoDeclaracoes) {
            return verificaRetornoBloco(blocoDeclaracoes);
        }

        // Recursão para comandos de bloco
        if (declaracao instanceof ComandoBloco comandoBloco) {
            return verificaRetornoBloco(comandoBloco.getDeclaracoes());
        }

        // Recursão para comandos 'se', incluindo verificações para 'se' e 'senão'
        if (declaracao instanceof ComandoSe comandoSe) {

            // Se não tem bloco 'senão', não é válido
            if (comandoSe.getAlternativa() == null) {
                return false;
            }

            // Os casos 'se' e 'senão' devem terminar com retorno
            return validaRetorno(comandoSe.getConsequencia()) && validaRetorno(comandoSe.getAlternativa());
        }

        // Não é um retorno válido
        return false;
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
    public String toString() {
        return "DeclaracaoFuncao {\n" +
                "nome='" + nome + '\'' +
                ",\ntipoRetorno=" + tipoRetorno +
                ",\nparametros=" + parametros +
                ",\ncorpo=" + corpo +
                "\n}\n";
    }
}
