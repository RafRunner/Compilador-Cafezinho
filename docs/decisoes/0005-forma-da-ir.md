# 0005. Forma da IR: três endereços, offsets concretos, slots de pilha simbólicos

- **Status:** Aceita
- **Data:** 2026-11-18

## Contexto

Com o layout pertencendo ao alvo (0004), é preciso decidir em que ponto offsets e tamanhos entram na IR. Opções:

1. **Offsets concretos no lowering:** o lowering AST → IR usa o `LayoutAlvo` e emite `load i32 [p + 8]`.
2. **Acesso simbólico na IR** (como o `getelementptr` do LLVM), resolvido por um passo tardio que conhece o layout.
3. **Duas IRs**, uma de alto nível com agregados e outra de baixo nível (estilo MLIR).

Na prática a IR de compiladores reais é ciente do alvo mas não da máquina: o módulo LLVM carrega uma string `datalayout` e o clang já gera IR específica do alvo; o GIMPLE do GCC também conhece tamanhos e offsets.

## Decisão

- **Forma:** código de três endereços com registradores virtuais ilimitados e `load`/`store` explícitos.
- **Tipos da IR:** `i8`, `i32`, `i64`, `f32`, `f64`, `ptr`. São larguras (semântica, 0003), não layout. Agregados não existem na IR: registros e vetores viram cálculo de endereço no lowering.
- **Offsets concretos (opção 1):** o lowering recebe o `LayoutAlvo` e emite offsets numéricos de campos e tamanhos de elementos.
- **Slots de pilha simbólicos:** variáveis locais são slots abstratos (`slot %x : Ponto`, com tamanho e alinhamento). O backend decide a posição de cada slot no frame (papel atual do `FrameMIPS32`), já que após a alocação de registradores muitas locais nem vão para a pilha.
- **Sem conhecimento da máquina:** a IR não sabe que registradores existem nem que `i64` não cabe num registrador. A legalização (`i64` em pares, `f64` em pares de registradores de ponto flutuante) é um passo do backend MIPS.

Divisão de conhecimento:

| onde              | o que sabe                                                              |
|-------------------|-------------------------------------------------------------------------|
| Semântico         | largura dos tipos (`int` = 32 bits), nada de bytes                      |
| Lowering AST → IR | `LayoutAlvo`: offsets de campos, tamanho de elemento de vetor           |
| IR                | `i8`/`i32`/`i64`/`f32`/`f64`/`ptr`, offsets concretos, slots simbólicos |
| Backend MIPS      | frame, registradores, legalização de `i64` e `f64`                       |

## Alternativas consideradas

- **Acesso simbólico (opção 2):** IR portável, mas as otimizações enxergam menos até o passo que resolve os acessos. Sem um segundo backend, a portabilidade não compensa.
- **Duas IRs (opção 3):** complexidade excessiva para um único backend.

## Consequências

- A IR é gerada de novo para cada alvo; aceitável, pois compila-se para um alvo por vez.
- Constant folding e CSE atuam diretamente sobre a aritmética de endereços.
- Se surgir um segundo alvo com layout muito diferente, a opção 2 pode ser revisitada num novo registro.
