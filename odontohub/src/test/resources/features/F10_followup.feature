# language: pt

Funcionalidade: Execução de Protocolos de Pós-Operatório Ativo
  Como Recepcionista
  Eu quero receber tarefas automáticas para ligar para pacientes que passaram por cirurgias recentes
  Para monitorar dores ou complicações precocemente

  Contexto:
    Dado que o paciente "Gabriela Souza" está cadastrado no sistema
    E que o dentista "Dr. Carlos" é o responsável pelo atendimento

  Cenário: Criação automática de tarefas de follow-up ao concluir uma cirurgia
    Dado que o dentista realizou um procedimento do tipo "Cirurgia" para "Gabriela Souza"
    Quando o sistema processa os gatilhos de pós-operatório
    Então deve ser criada uma tarefa de ligação de 24h para "Gabriela Souza"
    E deve ser criada uma tarefa de ligação de 72h para "Gabriela Souza"

  Cenário: Preenchimento do checklist de 24 horas sem intercorrências
    Dado que existe uma tarefa de follow-up de 24h pendente para "Gabriela Souza"
    Quando a recepcionista registra o checklist informando sangramento "Não" e nível de dor "2"
    Então o checklist de 24h deve ser salvo com os dados informados
    E a tarefa de ligação de 24h deve ser marcada como concluída

  Cenário: Escalonamento de emergência quando nível de dor é maior que 7
    Dado que existe uma tarefa de follow-up de 24h pendente para "Gabriela Souza"
    Quando a recepcionista registra o checklist com nível de dor "8" e sangramento "Não"
    Então o sistema deve acionar um alerta de emergência
    E o dentista "Dr. Carlos" deve ser notificado imediatamente

  Cenário: Escalonamento de emergência por sangramento ativo independente do nível de dor
    Dado que existe uma tarefa de follow-up de 72h pendente para "Gabriela Souza"
    Quando a recepcionista registra o checklist informando sangramento "Sim" e nível de dor "3"
    Então o sistema deve acionar um alerta de emergência para o dentista responsável

  Cenário: Procedimento não cirúrgico não gera follow-up
    Dado que o dentista realizou o procedimento "Profilaxia" para "Gabriela Souza"
    Quando o sistema processa os gatilhos de pós-operatório
    Então nenhuma tarefa de follow-up deve ser criada para "Gabriela Souza"
