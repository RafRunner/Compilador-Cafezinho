package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.ast.comandos.*;
import src.raiz.ast.declaracoes.DeclaracaoVariavelEmBloco;
import src.raiz.ast.expressoes.*;
import src.raiz.ast.expressoes.Expressao;
import src.raiz.compilador.tabeladesimbolos.TabelaDeSimbolos;
import src.raiz.erros.ErroSemantico;

public interface VisitadorDeNos {

    void visitarPorgrama();

    void visitarDeclaracaoFuncaoEVariaveis(src.raiz.ast.declaracoes.DeclaracaoFuncoesEVariaveis node);

    void visitarBlocoPrograma(src.raiz.ast.declaracoes.BlocoPrograma blocoPrograma);

    void visitarEscopo(src.raiz.ast.declaracoes.BlocoDeclaracoes blocoDeclaracoes, TabelaDeSimbolos tabelaDoEscopo);

    TipoVariavel visitarExpressao(Expressao expressao, TabelaDeSimbolos tabelaDoEscopo);

    void visitarDeclaracaoDeVariaveisEmBloco(DeclaracaoVariavelEmBloco node, TabelaDeSimbolos tabelaBloco);

    TipoVariavel visitaIdentificador(ExpressaoIdentificador identificador, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoInteiroLiteral(ExpressaoInteiroLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoFlutuanteLiteral(ExpressaoFlutuanteLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoCaractereLiteral(ExpressaoCaractereLiteral expressao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoStringLiteral(ExpressaoStringLiteral expressao, TabelaDeSimbolos tabela);

    void visitarComandoNovalinha(ComandoNovalinha comandoNovalinha);

    TipoVariavel visitarExpressaoSoma(ExpressaoMais expressaoMais, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoSubtracao(ExpressaoMenos expressaoMenos, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoVezes(ExpressaoVezes expressaoVezes, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoDivisao(ExpressaoDivisao expressaoDivisao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoResto(ExpressaoResto expressaoResto, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoE(ExpressaoE expressaoE, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoOu(ExpressaoOu expressaoOu, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoIgual(ExpressaoIgual expressaoIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoDiferente(ExpressaoDiferente expressaoDiferente, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMaior(ExpressaoMaior expressaoMaior, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMaiorIgual(ExpressaoMaiorIgual expressaoMaiorIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMenor(ExpressaoMenor expressaoMenor, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoMenorIgual(ExpressaoMenorIgual expressaoMenorIgual, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoAtribuicao(ExpressaoAtribuicao expressaoAtribuicao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoTernaria(ExpressaoTernaria expressaoTernaria, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoNegacao(ExpressaoNegacao expressaoNegacao, TabelaDeSimbolos tabela);

    TipoVariavel visitarExpressaoNegativo(ExpressaoNegativo expressaoNegativo, TabelaDeSimbolos tabela);

    void visitarComandoEscreva(ComandoEscreva comandoEscreva, TabelaDeSimbolos tabela);

    TipoVariavel visitaExpressaoChamadaFuncao(ExpressaoChamadaFuncao expressaoChamadaFuncao, TabelaDeSimbolos tabela);

    void visitarComandoRetorno(ComandoRetorno comandoRetorno, TabelaDeSimbolos tabela);

    void visitarComandoSe(ComandoSe comandoSe, TabelaDeSimbolos tabela);

    void visitarComandoEnquanto(ComandoEnquanto comandoEnquanto, TabelaDeSimbolos tabela);

    void visitarComandoLeia(ComandoLeia comandoLeia, TabelaDeSimbolos tabela);

    // Funções nativas
    default TipoVariavel visitarFuncaoNativa(ExpressaoChamadaFuncao chamada, FuncoesNativas funcao, TabelaDeSimbolos tabela) {
        if (funcao.parametros.size() != chamada.getArgumentos().size()) {
            throw new ErroSemantico(
                    "Função nativa " + funcao.nome + " espera receber " + funcao.parametros.size()
                    + " argumento(s), mas recebeu " + chamada.getArgumentos().size(),
                    chamada.getToken()
            );
        }

        int i = 0;
        for (ParametroFuncaoNativa parametro : funcao.parametros) {
            TipoVariavel tipoArgumento = visitarExpressao(chamada.getArgumentos().get(i), tabela);

            if (tipoArgumento != parametro.getTipo()) {
                throw new ErroSemantico(
                        parametro.getNome() + " posição " + (i + 1) + " espera argumento do tipo "
                        + parametro.getTipo() + " mas recebeu do tipo " + tipoArgumento,
                        chamada.getToken()
                );
            }

            i++;
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
