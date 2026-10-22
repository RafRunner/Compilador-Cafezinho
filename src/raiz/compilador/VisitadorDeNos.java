package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.artificiais.ExpressaoLeia;
import src.raiz.ast.artificiais.ExpressaoVazio;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.*;
import src.raiz.ast.expressoes.*;
import src.raiz.compilador.tabeladesimbolos.TabelaDeSimbolos;
import src.raiz.erros.BugCompilador;
import src.raiz.erros.ErroSemantico;

import java.util.List;

public interface VisitadorDeNos {

    void visitarPorgrama();

    void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node);

    void visitarBlocoPrograma(BlocoPrograma blocoPrograma);

    void visitarEscopo(BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaDoEscopo);

    default TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo) {
        return switch (expressao) {
            case ExpressaoEntreParenteses expressaoEntre ->
                    visitarExpressao(expressaoEntre.getExpressao(), tabelaDoEscopo);
            case ExpressaoIdentificador expressaoIdentificador ->
                    visitaIdentificador(expressaoIdentificador, tabelaDoEscopo);
            case ExpressaoInteiroLiteral intLiteral -> visitarExpressaoInteiroLiteral(intLiteral, tabelaDoEscopo);
            case ExpressaoFlutuanteLiteral flutLiteral -> visitarExpressaoFlutuanteLiteral(flutLiteral, tabelaDoEscopo);
            case ExpressaoCaractereLiteral carVirtual -> visitarExpressaoCaractereLiteral(carVirtual, tabelaDoEscopo);
            case ExpressaoStringLiteral stringLiteral -> visitarExpressaoStringLiteral(stringLiteral, tabelaDoEscopo);
            case ExpressaoAtribuicao expressaoAtribuicao ->
                    visitarExpressaoAtribuicao(expressaoAtribuicao, tabelaDoEscopo);
            case ExpressaoBinaria expressaoBinaria -> visitarExpressaoBinaria(expressaoBinaria, tabelaDoEscopo);
            case ExpressaoTernaria expressaoTernaria -> visitarExpressaoTernaria(expressaoTernaria, tabelaDoEscopo);
            case ExpressaoUnaria expressaoUnaria -> visitarExpressaoUnaria(expressaoUnaria, tabelaDoEscopo);
            case ExpressaoLeia expressaoLeia -> visitarExpressaoLeia(expressaoLeia, tabelaDoEscopo);
            case ExpressaoVazio ignored -> TipoVariavel.VAZIO;
            case ExpressaoChamadaFuncao chamadaFuncao -> visitaExpressaoChamadaFuncao(chamadaFuncao, tabelaDoEscopo);
            default -> throw new BugCompilador("Tipo de expressão não identificada " + expressao.getClass());
        };
    }

    void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco);

    TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoFlutuanteLiteral(ExpressaoFlutuanteLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela);

    void visitarComandoNovalinha(ComandoNovalinha comandoNovalinha);

    TipoVariavel visitarExpressaoBinaria(ExpressaoBinaria expressaoBinaria, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoTernaria(ExpressaoTernaria expressaoTernaria, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoUnaria(ExpressaoUnaria expressaoUnaria, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoLeia(ExpressaoLeia expressaoLeia, TabelaDeSimbolos tabela);

    void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela);

    TipoVariavel visitaExpressaoChamadaFuncao(ExpressaoChamadaFuncao expressaoChamadaFuncao, TabelaDeSimbolos tabela);

    void visitarComandoRetorno(ComandoRetorno comandoRetorno, TabelaDeSimbolos tabela);

    void visitarComandoSe(ComandoSe comandoSe, TabelaDeSimbolos tabela);

    void visitarComandoEnquanto(ComandoEnquanto comandoEnquanto, TabelaDeSimbolos tabela);

    void visitarComandoLeia(ComandoLeia comandoLeia, TabelaDeSimbolos tabela);

    // Funções nativas
    default TipoVariavel visitarFuncaoNativa(ExpressaoChamadaFuncao chamada, FuncoesNativas funcao, TabelaDeSimbolos tabela) {
        List<Expressao> argumentos = chamada.getArgumentos();
        if (funcao.parametros.size() != argumentos.size()) {
            String mensagemErro = String.format("Função nativa '%s' espera receber %d argumento(s), mas recebeu %d",
                    funcao.nome, funcao.parametros.size(), argumentos.size());
            throw new ErroSemantico(mensagemErro, chamada.getToken());
        }

        for (int i = 0; i < funcao.parametros.size(); i++) {
            ParametroFuncaoNativa parametro = funcao.parametros.get(i);
            TipoVariavel tipoArgumento = visitarExpressao(argumentos.get(i), tabela);

            if (tipoArgumento != parametro.getTipo()) {
                String mensagemErro = String.format("'%s' na posição %d espera argumento do tipo %s mas recebeu do tipo %s",
                        parametro.getNome(), i + 1, parametro.getTipo(), tipoArgumento);
                throw new ErroSemantico(mensagemErro, chamada.getToken());
            }
        }        

        switch (funcao) {
            case PISO -> visitarFuncaoPiso(tabela);
            case RAND -> visitarFuncaoRand(tabela);
        }

        return funcao.tipoRetorno;
    }

    void visitarFuncaoRand(TabelaDeSimbolos tabela);

    void visitarFuncaoPiso(TabelaDeSimbolos tabela);
}
