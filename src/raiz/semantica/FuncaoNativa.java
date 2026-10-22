package src.raiz.semantica;

import src.raiz.ast.TipoVariavel;

import java.util.List;

// Funções que já existem na linguagem, sem precisar ser declaradas. Aqui fica só o que a linguagem define:
// nome e assinatura. Como cada uma é implementada (código no lugar da chamada, rotina, etc) é decisão do backend
public enum FuncaoNativa {

    RAND("rand", TipoVariavel.INTEIRO, List.of(new ParametroFuncaoNativa(TipoVariavel.INTEIRO, "limiteSuperior"))),
    PISO("piso", TipoVariavel.INTEIRO, List.of(new ParametroFuncaoNativa(TipoVariavel.FLUTUANTE, "valor")));

    public final String nome;
    public final TipoVariavel tipoRetorno;
    public final List<ParametroFuncaoNativa> parametros;

    FuncaoNativa(String nome, TipoVariavel tipoRetorno, List<ParametroFuncaoNativa> parametros) {
        this.nome = nome;
        this.tipoRetorno = tipoRetorno;
        this.parametros = parametros;
    }
}
