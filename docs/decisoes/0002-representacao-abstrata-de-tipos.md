# 0002. Tipos como hierarquia de classes, vetor como tipo

- **Status:** Aceita
- **Data:** 2026-11-18

## Contexto

O tipo de uma variável é o enum `TipoVariavel` (`INTEIRO`, `FLUTUANTE`, `CARACTERE`, `VAZIO`, `STRING`), e vetor não faz parte do tipo: é um `tamanhoVetor` solto em `Variavel`.
Assim não dá para expressar vetor de registro, registro com campo vetor, ponteiro para algo, nem comparar dois tipos compostos.

## Decisão

Substituir o enum por uma hierarquia de classes imutáveis, por exemplo:

- `Tipo` (abstrata): igualdade estrutural ou nominal, compatibilidade, promoção.
- `TipoPrimitivo`: `car`, `int`, `flut`, `vazio` e, mais tarde, `longo` e `duplo`.
- `TipoVetor(elemento, tamanho)`: tamanho pode ser desconhecido em parâmetros (`int v[]`).
- `TipoRegistro(nome, campos)`: tipos do usuário, com igualdade nominal.
- `TipoPonteiro(alvo)`: se/quando a linguagem ganhar ponteiros ou referências.

Vetor passa a ser um tipo; `tamanhoVetor` sai de `Variavel`.
Tipos definidos pelo usuário ficam numa tabela de tipos própria, num namespace separado do das variáveis e funções.

`Tipo` **não** tem `tamanho()` nem `alinhamento()` em bytes: isso é do alvo (ver 0004). A largura em bits dos primitivos, sim, é propriedade do tipo (ver 0003).

A hierarquia já prevê `longo` e `duplo` e as regras de promoção entre eles, mesmo antes de a gramática aceitá-los.

## Consequências

- Primeiro passo de 0001: refatoração sem features novas, adaptando análise semântica e backend atual.
- O semântico passa a raciocinar sobre tipos compostos de forma uniforme (indexação produz o tipo do elemento, acesso a campo produz o tipo do campo).
