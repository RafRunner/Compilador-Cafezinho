package src.raiz.ast.declaracoes;

import src.raiz.util.AstUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

// Classe que representa a parte inicial do programa, onde declaramos variáveis globais
// e funções.
public class DeclaracaoFuncoesEVariaveis extends Declaracao {

    private final List<DeclaracaoDeVariavel> declaracoesDeVariaveis = new LinkedList<>();
    private final List<DeclaracaoFuncao> declaracoesDeFuncoes = new LinkedList<>();

    public DeclaracaoFuncoesEVariaveis() {
        super(null);
    }

    public List<DeclaracaoDeVariavel> getDeclaracoesDeVariaveis() {
        return declaracoesDeVariaveis;
    }

    public List<DeclaracaoFuncao> getDeclaracoesDeFuncoes() {
        return declaracoesDeFuncoes;
    }

    // Retorna as declarações de funções e variáveis globais na ordem que aparecem no programa
    public List<DeclaracaoGlobal> getDeclaracoesEmOrdem() {
        List<DeclaracaoGlobal> todas = new ArrayList<>(this.declaracoesDeVariaveis);
        todas.addAll(this.declaracoesDeFuncoes);

        todas.sort(Comparator.comparing(DeclaracaoGlobal::getToken));

        return todas;
    }

    @Override
    public String codigoOriginal() {
        List<DeclaracaoGlobal> declaracoes = getDeclaracoesEmOrdem();
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < declaracoes.size(); i++) {
            if (i > 0) {
                // Variáveis globais seguidas ficam juntas; uma função é separada das vizinhas por uma linha em branco
                boolean variaveisSeguidas = declaracoes.get(i - 1) instanceof DeclaracaoDeVariavel
                        && declaracoes.get(i) instanceof DeclaracaoDeVariavel;
                codigo.append(variaveisSeguidas ? "\n" : "\n\n");
            }
            codigo.append(declaracoes.get(i).codigoOriginal());
        }

        return codigo.toString();
    }

    @Override
    public String representacaoArvore(int profundidade) {
        return "DeclaracaoFuncoesEVariaveis {\n"
               + getIndentacao(profundidade) + "declaracoesDeVariaveis: " + AstUtil.representacoesArvore(declaracoesDeVariaveis, profundidade + 1) + "\n"
               + getIndentacao(profundidade) + "declaracoesDeFuncoes: " + AstUtil.representacoesArvore(declaracoesDeFuncoes, profundidade + 1) + "\n"
               + getIndentacao(profundidade - 1) + "}";
    }
}
