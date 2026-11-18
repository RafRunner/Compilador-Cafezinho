# Registro de decisões

Decisões de arquitetura do compilador, no formato ADR (Architecture Decision Record).
Cada arquivo registra uma decisão: o contexto, o que foi decidido, as alternativas e as consequências.
Uma decisão não é editada depois de aceita; se mudar, um novo registro a substitui e o antigo é marcado como "Substituída por NNNN".

| Nº | Decisão | Status |
|----|---------|--------|
| [0001](0001-ordem-tipos-ir-otimizacoes.md) | Ordem: tipos no frontend, depois IR, depois codegen das extensões e otimizações | Aceita |
| [0002](0002-representacao-abstrata-de-tipos.md) | Tipos como hierarquia de classes, vetor como tipo | Aceita |
| [0003](0003-larguras-fixas-na-linguagem.md) | Larguras dos tipos primitivos fixadas pela linguagem | Aceita |
| [0004](0004-layout-de-memoria-pertence-ao-alvo.md) | Tamanho, alinhamento e layout de memória pertencem ao alvo (`LayoutAlvo`) | Aceita |
| [0005](0005-forma-da-ir.md) | Forma da IR: três endereços, offsets concretos, slots de pilha simbólicos | Aceita |
| [0006](0006-semantica-de-string.md) | Semântica de string | Em aberto |
