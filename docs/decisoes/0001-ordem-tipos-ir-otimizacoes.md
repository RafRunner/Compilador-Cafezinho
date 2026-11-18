# 0001. Ordem: tipos no frontend, depois IR, depois codegen das extensões e otimizações

- **Status:** Aceita
- **Data:** 2026-11-18

## Contexto

Duas frentes de trabalho competem:

- **Extensões da linguagem:** strings, tipos definidos pelo usuário (registros), e mais tarde `longo` e `duplo`.
- **Representação intermediária (IR):** hoje o backend traduz direto da AST para MIPS (`BackendMIPS32`). Uma IR permite alocação de registradores por coloração de grafos, peephole, constant folding etc.

Fazer as extensões completas primeiro obrigaria a escrever a geração de código delas duas vezes: no backend atual e depois no backend IR → MIPS.
Fazer a IR primeiro, sem saber quais tipos existirão, provavelmente produziria uma IR sem um bom modelo de memória (endereços, `load`/`store` com offset, valores que não cabem num registrador), que teria de ser redesenhada.

## Decisão

Intercalar as duas frentes, nesta ordem:

1. Refatorar a representação de tipos sem adicionar features (ver 0002).
2. Gramática e análise semântica de registros e strings, só no frontend: parse, tabela de tipos do usuário, acesso a campo, checagem de tipos.
3. Desenhar a IR já conhecendo esses tipos (ver 0005) e implementar o lowering AST → IR.
4. Backend IR → MIPS; aposentar o backend que traduz direto da AST. A geração de código das extensões é escrita uma única vez, aqui.
5. Otimizações sobre a IR: constant folding, peephole, alocação por coloração de grafos.

O alocador de registradores atual (`AlocadorRegistradores`) não recebe mais investimento: será substituído pela coloração de grafos.

## Alternativas consideradas

- **Extensões completas primeiro (até o assembly):** descartada pelo retrabalho no backend.
- **IR primeiro:** descartada pelo risco de redesenho quando agregados chegarem.

## Consequências

- Entre os passos 2 e 4, registros e strings são aceitos pelo frontend mas não geram código. O compilador deve rejeitá-los com erro claro no backend atual, ou os testes dessas features ficam restritos à análise.
- Os testes existentes servem de rede de segurança para os passos 1 e 4.
