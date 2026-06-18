Prompt — Evoluir Frontend da F06 (Esterilização) para uma funcionalidade completa mantendo o design atual do OdontoHub

Projeto: OdontoHub — Sistema de gestão de consultório odontológico
Stack Frontend: React + Vite + TypeScript

IMPORTANTE:
Antes de qualquer alteração, analise toda a estrutura atual do frontend e entenda o padrão visual já utilizado no projeto.

Leia obrigatoriamente:

* `src/app/pages/auxiliar/Esterilizacao.tsx`
* `src/app/routes.tsx`
* `src/app/App.tsx`
* componentes em:

  * `src/app/components/`
  * `src/app/components/ui/`
* páginas relacionadas:

  * `src/app/pages/auxiliar/`
  * `src/app/pages/recepcionista/`
  * `src/app/pages/dentista/`

Objetivo da tarefa:

A funcionalidade F06 — Esterilização de Instrumentos já existe visualmente, mas está muito básica.

Quero transformar essa área em um módulo completo, moderno e profissional, mantendo exatamente o mesmo padrão visual, componentes, identidade visual e organização do restante do OdontoHub.

IMPORTANTE:

* NÃO criar um design novo.
* NÃO trocar bibliotecas UI.
* NÃO mudar a identidade visual.
* NÃO quebrar rotas existentes.
* NÃO remover funcionalidades já existentes.
* NÃO refatorar o projeto inteiro.
* EVOLUIR a funcionalidade atual mantendo o padrão já utilizado no sistema.

A F06 possui as seguintes regras de negócio:

STATUS DE ESTERILIZAÇÃO:

* ESTERIL
* CONTAMINADO
* VENCIDO

STATUS DO INSTRUMENTO:

* ATIVO
* INATIVO

REGRAS IMPORTANTES:

* instrumentos possuem:

  * nome;
  * categoria;
  * código identificador;
  * prazo de validade;
  * status de esterilização;
  * status ativo/inativo;
* instrumentos podem:

  * ser cadastrados;
  * esterilizados;
  * contaminados;
  * desativados;
* instrumentos INATIVOS:

  * permanecem salvos para histórico;
  * não aparecem nas listas operacionais;
  * não podem sofrer operações;
  * devem ficar visualmente diferentes;
  * devem possuir ações desabilitadas.

O que implementar:

1. Melhorar completamente a tela principal de Esterilização

A tela deve parecer um módulo operacional real e não apenas uma tabela simples.

Adicionar:

* tabela/lista moderna de instrumentos;
* organização visual melhor;
* cards de resumo;
* indicadores rápidos;
* filtros;
* busca;
* ações visuais;
* badges/status visuais.

Indicadores desejados:

* total estéreis;
* total vencidos;
* total contaminados;
* total ativos;
* total inativos.

Filtros desejados:

* por status de esterilização;
* por categoria;
* por ativo/inativo;
* busca por nome;
* busca por código identificador.

2. Cadastro completo de instrumento

Adicionar fluxo visual completo para:

* cadastrar novo instrumento;
* informar:

  * nome;
  * categoria;
  * código identificador;
  * prazo de validade.

Regras:

* impedir código duplicado;
* mostrar mensagem amigável;
* atualizar a lista automaticamente após cadastro.

3. Operações do instrumento

Adicionar ações reais na interface para:

* marcar como Estéril;
* marcar como Contaminado;
* desativar instrumento;
* visualizar detalhes;
* visualizar status atual;
* visualizar validade;
* visualizar histórico simples.

IMPORTANTE:

* instrumentos INATIVOS não podem sofrer operações;
* botões devem aparecer desabilitados;
* visual deve indicar claramente que o item está inativo.

4. Modais e formulários

Adicionar:

* modal de cadastro;
* modal de esterilização;
* confirmação visual para desativação;
* mensagens de sucesso;
* mensagens de erro;
* loading visual simples se necessário.

5. Fluxo de navegação

Verifique cuidadosamente:

* sidebar;
* menu;
* botões de navegação;
* rotas;
* links internos;
* acesso à tela de esterilização.

Corrigir qualquer fluxo quebrado.

Garantir que:

* seja possível chegar normalmente à funcionalidade;
* os botões realmente funcionem;
* as telas estejam conectadas corretamente.

6. CRUD funcional no frontend mockado

Mesmo sem backend real:

* todos os botões devem possuir comportamento;
* nenhum botão deve ficar “fake”;
* nenhum modal deve abrir sem ação;
* nenhuma ação deve ficar incompleta.

Garantir funcionamento mockado para:

* cadastrar;
* cancelar;
* salvar;
* esterilizar;
* contaminar;
* desativar;
* buscar;
* filtrar;
* limpar filtros;
* abrir/fechar modais.

7. Estrutura e organização do código

Evitar colocar toda a lógica em um único arquivo.

Pode:

* criar componentes auxiliares;
* criar tabelas reutilizáveis;
* separar cards;
* criar hooks/helpers;
* organizar mocks;
* separar dados;
* melhorar organização visual.

Mas:

* manter o padrão arquitetural atual do projeto;
* manter consistência com o restante do OdontoHub.

8. Integração futura com backend

Mesmo sem API:

* organizar o frontend pensando em futura integração;
* deixar estrutura preparada para consumo de backend depois.

9. NÃO FAZER

* não adicionar backend;
* não criar API;
* não usar banco de dados;
* não alterar funcionalidades de outros módulos sem necessidade;
* não criar visual incompatível com o projeto;
* não trocar framework;
* não criar arquitetura completamente diferente do restante do sistema.

10. Resultado esperado

Ao final:

* a F06 deve parecer uma funcionalidade completa;
* moderna;
* navegável;
* organizada;
* visualmente rica;
* coerente com sistema odontológico real;
* consistente com o restante do OdontoHub;
* pronta para futura integração backend.
