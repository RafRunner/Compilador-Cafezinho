# 0004. Tamanho, alinhamento e layout de memória pertencem ao alvo

- **Status:** Aceita
- **Data:** 2026-11-18

## Contexto

Alinhamento (`duplo` alinha em 8 no MIPS o32, pode ser 4 em outro alvo), padding e offsets de campos de registros, tamanho de ponteiro e o modo como um valor ocupa registradores variam por alvo.
Colocar isso em `Tipo` amarraria o frontend a um único backend.

## Decisão

Criar uma interface do alvo, `LayoutAlvo`, no estilo do `DataLayout` do LLVM:

- `tamanho(Tipo)`: bytes ocupados em memória.
- `alinhamento(Tipo)`.
- `offsetCampo(TipoRegistro, campo)`: com o padding do alvo.

O primeiro implementador é o layout MIPS32 o32.
O analisador semântico não usa bytes. Um eventual operador `tamanho_de(T)` é resolvido no lowering, que tem o layout.
O lowering AST → IR **recebe** o `LayoutAlvo` de fora; não o fixa internamente.

## Consequências

- Um novo alvo (x86-64, RISC-V) pluga outro `LayoutAlvo` e reaproveita frontend, lowering e otimizações.
- O programa em IR gerado depende do alvo (ver 0005), mas a linguagem da IR e os passos de otimização não.
