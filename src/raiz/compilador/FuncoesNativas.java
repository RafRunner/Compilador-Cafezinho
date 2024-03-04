package src.raiz.compilador;

import src.raiz.ast.TipoVariavel;

import java.util.Collections;
import java.util.List;

public enum FuncoesNativas {

    RAND(
            "rand",
            TipoVariavel.INTEIRO,
            Collections.singletonList(new ParametroFuncaoNativa(TipoVariavel.INTEIRO, "limiteSuperior"))
    ),
    INTEIRO(
            "inteiro",
            TipoVariavel.INTEIRO,
            Collections.singletonList(new ParametroFuncaoNativa(TipoVariavel.FLUTUANTE, "valorFlutuante"))
    );

    public final String nome;
    public final TipoVariavel tipoRetorno;
    public final List<ParametroFuncaoNativa> parametros;

    FuncoesNativas(String nome, TipoVariavel tipoRetorno, List<ParametroFuncaoNativa> parametros) {
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
        this.parametros = parametros;
    }
}
