package src.raiz.semantica;

import src.raiz.ast.NoSintatico;
import src.raiz.ast.TipoVariavel;

// Símbolos que guardam valores: variáveis globais, locais e parâmetros de funções
public sealed interface SimboloVariavel extends Simbolo
        permits SimboloVariavelGlobal, SimboloVariavelLocal, SimboloParametro {

    TipoVariavel getTipo();

    boolean isVetor();

    // Nó onde a variável foi declarada. Identifica a variável de forma única, mesmo com nomes repetidos em escopos diferentes
    NoSintatico getDeclaracao();
}
