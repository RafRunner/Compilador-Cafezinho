# 0003. Larguras dos tipos primitivos fixadas pela linguagem

- **Status:** Aceita
- **Data:** 2026-11-18

## Contexto

"Tamanho" de um tipo mistura duas coisas:

- **Largura do valor:** quantos bits o valor tem. Decide faixa, overflow e precisão, ou seja, o comportamento observável do programa.
- **Representação em memória:** bytes ocupados, alinhamento, padding (ver 0004).

Em C a largura varia por alvo (`long` tem 32 ou 64 bits), e o mesmo programa pode dar resultados diferentes.

## Decisão

A linguagem fixa a largura dos primitivos, como Java e Rust:

| tipo    | largura                |
|---------|------------------------|
| `car`   | 8 bits                 |
| `int`   | 32 bits, inteiro       |
| `longo` | 64 bits, inteiro       |
| `flut`  | 32 bits, IEEE 754      |
| `duplo` | 64 bits, IEEE 754      |

A largura é semântica: o analisador usa-a para conversões e promoções (`int` + `longo` → `longo`), sem consultar o alvo.

## Consequências

- Programas de teste dão o mesmo resultado em qualquer backend.
- Um alvo de 32 bits precisa emular `longo` (no MIPS32: pares de registradores, carry manual, rotina de runtime para divisão/resto de 64 bits). Isso fica na legalização do backend (ver 0005).
