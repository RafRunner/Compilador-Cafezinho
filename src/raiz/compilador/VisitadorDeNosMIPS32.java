package src.raiz.compilador;

import src.raiz.ast.*;
import src.raiz.compilador.tabeladesimbolos.*;

import java.util.List;

public class VisitadorDeNosMIPS32 implements VisitadorDeNos {

    private final TabelaDeSimbolos tabelaDeSimbolos;
    private final GeradorDeCodigo gerador;

    public VisitadorDeNosMIPS32(TabelaDeSimbolos tabelaDeSimbolos, GeradorDeCodigo gerador) {
        this.tabelaDeSimbolos = tabelaDeSimbolos;
        this.gerador = gerador;
    }

    @Override
    public void visitarDeclaracaoFuncaoEVariaveis(DeclaracaoFuncoesEVariaveis node) {
        // Processar declarações de variáveis globais
        for (DeclaracaoDeVariavel declaracao : node.getDeclaracoesDeVariaveis()) {
            for (Variavel var : declaracao.getVariaveis()) {
                tabelaDeSimbolos.adicionaSimbolo(new SimboloVariavelGlobal(var));

                String nomeVariavel = var.getNome();
                TipoVariavelNo tipo = var.getTipo();
                Integer tamanhoVetor = var.getTamanhoVetor();

                // Gerar código para variável global
                if (tamanhoVetor == null) { // Não é um vetor
                    gerador.gerarVarGlobal(nomeVariavel + ": .space " + getEspacoMemoria(tipo));
                } else { // É um vetor
                    gerador.gerarVarGlobal(nomeVariavel + ": .space " + getEspacoMemoria(tipo) * tamanhoVetor);
                }
            }
        }

        for (DeclaracaoFuncao funcao : node.getDeclaracoesDeFuncoes()) {
            String nomeFuncao = funcao.getNome();
            List<ParametroFuncao> parametros = funcao.getParametros();

            // Tabela que contêm as variáveis locais da função, incluindo os parâmetros de chamada
            TabelaDeSimbolos simbolosFuncao = tabelaDeSimbolos.criaBlocoInterno();

            tabelaDeSimbolos.adicionaSimbolo(new SimboloFuncao(funcao, simbolosFuncao));

            // Gerar etiqueta (label) da função
            gerador.gerarFuncao(nomeFuncao + ":");

            // Prólogo da função
            gerador.gerarFuncao("addiu $sp, $sp, -4");
            gerador.gerarFuncao("sw    $ra, 0($sp)");

            // Configurar os parâmetros
            // Por exemplo, alocar espaço para cada parâmetro no frame da função
            int offset = -8; // Começa após salvar $ra e $fp
            for (ParametroFuncao param : parametros) {
                tabelaDeSimbolos.adicionaSimbolo(new SimboloParametroFuncao(param, offset));
                simbolosFuncao.alteraOffset(offset);
                offset -= getEspacoMemoria(param.getTipo());
            }

            // Gerar código do corpo da função

            // Epílogo da função
            gerador.gerarFuncao("lw    $ra, 0($sp)");
            gerador.gerarFuncao("addiu $sp, $sp, 4");
            gerador.gerarFuncao("jr    $ra");
        }
    }

    private int getEspacoMemoria(TipoVariavelNo tipo) {
        switch (tipo.getTipo()) {
            case INTEIRO:
                return 4;   // int tem 4 bytes -> 32 bits
            case CARACTERE:
                return 1;   // um caractere é um byte
            default:
                return 0;
        }
    }

    private void leVariavelGlobal(Simbolo simbolo, RegistradoresMIPS32 registrador) {
        gerador.gerarVarGlobal("la $t1, " + simbolo.getNome());            // carrega endereço da variável em $t1
        gerador.gerarVarGlobal("lw " + registrador.getNome() + " 0($t1)"); // carrega o valor no endereço no registrador passado
    }
}
