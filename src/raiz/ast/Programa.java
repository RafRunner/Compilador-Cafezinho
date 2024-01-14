package src.raiz.ast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        StringBuilder sb = new StringBuilder("Programa {\n");
        StringBuilder comIndentacao = new StringBuilder();

        AstUtil.toStrings(sb, this.getDeclaracoes(), ", ");

        sb.append("}");
        String[] linhas = sb.toString().split("\n");

        int nivel = 0;
        for (String linha : linhas) {
            for (int j = 0; j < nivel; j++) {
                comIndentacao.append("  ");
            }
            if (linha.trim().endsWith("{")) {
                nivel++;
            } else if (linha.trim().endsWith("}")) {
                nivel--;
            }
            comIndentacao.append(linha).append("\n");
        }

        return comIndentacao.toString();
    }

    public String programaOriginal() {
        StringBuilder sb = new StringBuilder();
        AstUtil.representacoesString(sb, this.getDeclaracoes(), "\n");
        return sb.toString();
    }

    private List<Declaracao> getDeclaracoes() {
        return Arrays.asList(declaracaoFuncoesEVariaveis, blocoPrograma);
    }
}
