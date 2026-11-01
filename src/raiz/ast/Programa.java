package src.raiz.ast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import src.raiz.ast.declaracoes.BlocoPrograma;
import src.raiz.ast.declaracoes.Declaracao;
import src.raiz.ast.declaracoes.DeclaracaoFuncoesEVariaveis;
import src.raiz.token.Token;
import src.raiz.util.AstUtil;

// Classe que representa um programa completo. É a soma da declaração de Variáveis globais e funções
// seguido do bloco programa { ... }
public class Programa {

    private DeclaracaoFuncoesEVariaveis declaracaoFuncoesEVariaveis;
    private BlocoPrograma blocoPrograma;
    private final List<String> errosSemanticos = new LinkedList<>();

    public void setDeclaracaoFuncoesEVariaveis(DeclaracaoFuncoesEVariaveis declaracaoFuncoesEVariaveis) {
        this.declaracaoFuncoesEVariaveis = declaracaoFuncoesEVariaveis;
    }

    public DeclaracaoFuncoesEVariaveis getDeclaracaoFuncoesEVariaveis() {
        return declaracaoFuncoesEVariaveis;
    }

    public BlocoPrograma getBlocoPrograma() {
        return blocoPrograma;
    }

    public void setBlocoPrograma(BlocoPrograma blocoPrograma) {
        this.blocoPrograma = blocoPrograma;
    }

    public List<String> getErrosSemanticos() {
        return errosSemanticos;
    }

    public void reportaErroSemantico(String mensagem, Token token) {
        errosSemanticos.add(AstUtil.montaMensagemErro(mensagem, token));
    }

    @Override
    public String toString() {
        return "Programa " + AstUtil.representacoesArvore(this.getDeclaracoes(), 1);
    }

    // A indentação já vem pronta: cada bloco indenta o próprio conteúdo
    public String programaOriginal() {
        String globais = declaracaoFuncoesEVariaveis.codigoOriginal();
        String separador = globais.isEmpty() ? "" : "\n\n";
        return globais + separador + blocoPrograma.codigoOriginal() + "\n";
    }

    private List<Declaracao> getDeclaracoes() {
        return Arrays.asList(declaracaoFuncoesEVariaveis, blocoPrograma);
    }
}
