Prompt — Evoluir Frontend da F01 (Agendamento) para uma funcionalidade completa mantendo o design atual do OdontoHub

Projeto: OdontoHub — Sistema de gestão de consultório odontológico
Stack Frontend: React + Vite + TypeScript

IMPORTANTE:
Antes de qualquer alteração, analise toda a estrutura atual do frontend e entenda o padrão visual já utilizado no projeto.

Leia obrigatoriamente:

* páginas relacionadas à agenda/agendamento;
* `src/app/routes.tsx`
* `src/app/App.tsx`
* componentes em:

  * `src/app/components/`
  * `src/app/components/ui/`
* páginas relacionadas:

  * `src/app/pages/recepcionista/`
  * `src/app/pages/dentista/`
  * `src/app/pages/`

Objetivo da tarefa:

A funcionalidade F01 — Agendamento de Consultas e Retornos já existe visualmente, mas precisa ser revisada e evoluída para parecer um módulo completo, moderno e profissional, mantendo exatamente o mesmo padrão visual, componentes, identidade visual e organização do restante do OdontoHub.

IMPORTANTE:

* NÃO criar um design novo.
* NÃO trocar bibliotecas UI.
* NÃO mudar a identidade visual.
* NÃO quebrar rotas existentes.
* NÃO remover funcionalidades já existentes.
* NÃO refatorar o projeto inteiro.
* EVOLUIR a funcionalidade atual mantendo o padrão já utilizado no sistema.

A F01 possui as seguintes regras de negócio:

TIPOS DE AGENDAMENTO:

* CONSULTA
* RETORNO

STATUS DO AGENDAMENTO:

* AGENDADO
* CONFIRMADO
* CANCELADO
* REMARCADO

REGRAS IMPORTANTES:

* o sistema deve permitir:

  * criar agendamentos;
  * visualizar detalhes;
  * confirmar;
  * cancelar;
  * remarcar;
  * atualizar status;
  * visualizar histórico simples;

* cada agendamento deve possuir:

  * paciente;
  * dentista;
  * data;
  * horário;
  * tipo;
  * status;
  * responsável pela alteração;
  * data da última alteração;

* o sistema deve:

  * impedir conflito de horário para o mesmo dentista;
  * impedir datas passadas;
  * identificar automaticamente Consulta ou Retorno;
  * bloquear pacientes inadimplentes;
  * registrar alterações de status.

CLASSIFICAÇÃO AUTOMÁTICA:

* pacientes com Plano de Tratamento ativo:

  * RETORNO
* pacientes sem plano ativo:

  * CONSULTA

BLOQUEIO POR INADIMPLÊNCIA:

* pacientes inadimplentes não podem agendar sem autorização.

O que implementar:

1. Melhorar completamente a tela principal da agenda

A tela deve parecer um módulo operacional real e não apenas uma tabela simples.

Adicionar:

* calendário visual;
* tabela/lista moderna de agendamentos;
* organização visual melhor;
* cards de resumo;
* indicadores rápidos;
* filtros;
* busca;
* badges/status visuais;
* visualização clara por status.

Indicadores desejados:

* total agendados;
* total confirmados;
* total cancelados;
* total remarcados;
* total retornos;
* total consultas.

Filtros desejados:

* por dentista;
* por status;
* por tipo;
* por data;
* busca por paciente;
* busca por horário.

2. Cadastro completo de agendamento

Adicionar fluxo visual completo para:

* criar novo agendamento;
* selecionar paciente;
* selecionar dentista;
* selecionar data;
* selecionar horário;
* visualizar tipo automático;
* visualizar status inicial.

Regras:

* impedir conflito de horário;
* impedir data passada;
* mostrar mensagens amigáveis;
* atualizar a agenda automaticamente após cadastro.

3. Operações do agendamento

Adicionar ações reais na interface para:

* confirmar agendamento;
* cancelar agendamento;
* remarcar agendamento;
* visualizar detalhes;
* visualizar histórico;
* atualizar status.

IMPORTANTE:

* cancelamentos devem exigir motivo;
* remarcações devem atualizar data/horário;
* alterações devem registrar responsável e data;
* status devem atualizar visualmente.

4. Bloqueio por inadimplência

Adicionar comportamento visual/mockado para:

* paciente inadimplente;
* bloqueio de agendamento;
* mensagem amigável explicando o bloqueio.

5. Modais e formulários

Adicionar:

* modal de criação;
* modal de confirmação;
* modal de cancelamento;
* modal de remarcação;
* modal de detalhes;
* mensagens de sucesso;
* mensagens de erro;
* loading visual simples se necessário.

6. Fluxo de navegação

Verifique cuidadosamente:

* sidebar;
* menu;
* botões de navegação;
* rotas;
* links internos;
* acesso à agenda.

Corrigir qualquer fluxo quebrado.

Garantir que:

* seja possível chegar normalmente à funcionalidade;
* os botões realmente funcionem;
* as telas estejam conectadas corretamente.

7. CRUD funcional no frontend mockado

Mesmo sem backend real:

* todos os botões devem possuir comportamento;
* nenhum botão deve ficar “fake”;
* nenhum modal deve abrir sem ação;
* nenhuma ação deve ficar incompleta.

Garantir funcionamento mockado para:

* criar;
* editar;
* confirmar;
* cancelar;
* remarcar;
* buscar;
* filtrar;
* limpar filtros;
* abrir/fechar modais.

8. Estrutura e organização do código

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

9. Integração futura com backend

Mesmo sem API:

* organizar o frontend pensando em futura integração;
* deixar estrutura preparada para consumo de backend depois.

10. Verificações obrigatórias

Verifique cuidadosamente:

* se todos os botões possuem ação;
* se todos os modais funcionam;
* se existem fluxos quebrados;
* se existem estados inconsistentes;
* se os filtros realmente funcionam;
* se os status atualizam corretamente;
* se existe feedback visual para ações importantes;
* se o calendário e a agenda permanecem sincronizados.

11. NÃO FAZER

* não adicionar backend;
* não criar API;
* não usar banco de dados;
* não alterar funcionalidades de outros módulos sem necessidade;
* não criar visual incompatível com o projeto;
* não trocar framework;
* não criar arquitetura completamente diferente do restante do sistema.

12. Resultado esperado

Ao final:

* a F01 deve parecer uma funcionalidade completa;
* moderna;
* navegável;
* organizada;
* visualmente rica;
* coerente com sistema odontológico real;
* consistente com o restante do OdontoHub;
* pronta para futura integração backend.
