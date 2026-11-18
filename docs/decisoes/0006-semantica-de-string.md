# 0006. Semântica de string

- **Status:** Em aberto
- **Data:** 2026-11-18

## Contexto

Hoje `STRING` existe só como tipo interno (literais em `escreva`). Tornar string um tipo da linguagem exige escolher sua semântica, a decisão mais cara entre as extensões, porque pode exigir uma runtime.

## Opções

1. **Estilo C: string = vetor de `car` terminado em zero, mais literais.** Barato, sem runtime; encaixa na hierarquia de 0002 como `TipoVetor(car, n)`. Sem concatenação nativa nem tamanho dinâmico.
2. **String como valor, com concatenação e tamanho dinâmico.** Exige heap (`sbrk`, syscall 9 no SPIM/MARS) e uma pequena runtime em assembly; a IR passa a ter chamadas a rotinas de runtime. Abre questões de cópia/compartilhamento e liberação de memória.

## Decisão

Pendente. Precisa ser tomada antes do passo 3 de 0001 (desenho da IR), pois a opção 2 afeta a IR.
