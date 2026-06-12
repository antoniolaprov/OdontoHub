# language: pt

Funcionalidade: Registro de Nao Comparecimento (No-Show)
  Como recepcionista
  Eu quero registrar quando o paciente nao comparece a uma consulta confirmada
  Para diferenciar faltas de cancelamentos e apoiar a gestao da agenda

  Contexto:
    Dado que o dentista "Dr. Carlos" esta cadastrado no sistema
    E que o paciente "Joao Silva" esta cadastrado no sistema

  Cenario: Marcacao de no-show para paciente ausente em consulta confirmada
    Dado que existe um agendamento com status "Confirmado" para "Joao Silva" em "10/05/2026 09:00"
    Quando a recepcionista "Ana" marca o agendamento como "Nao Compareceu"
    Entao o status do agendamento deve ser "Nao Compareceu"
    E o responsavel pela alteracao deve ser registrado como "Ana"

  Cenario: Bloqueio de marcacao de no-show antes do horario da consulta
    Dado que existe um agendamento com status "Confirmado" para "Joao Silva" em "20/05/2026 16:00"
    Quando a recepcionista tenta marcar no-show antes do horario da consulta
    Entao o sistema deve rejeitar a marcacao de no-show
    E a mensagem de erro deve informar "Nao e permitido marcar no-show antes do horario da consulta"

  Cenario: No-show e contabilizado separadamente de cancelamento
    Dado que "Joao Silva" possui um agendamento cancelado e um agendamento marcado como "Nao Compareceu"
    Quando o dentista consulta o resumo de ocorrencias da agenda
    Entao a quantidade de no-show deve ser exibida separadamente da quantidade de cancelamentos

  Cenario: Paciente com reincidencia de no-show fica sinalizado para acompanhamento
    Dado que "Joao Silva" possui 2 registros anteriores de no-show nos ultimos 6 meses
    Quando a recepcionista registra um novo no-show para "Joao Silva"
    Entao o paciente deve ser sinalizado para acompanhamento
    E a sinalizacao deve informar "Paciente com recorrencia de faltas"

  Cenario: Agendamento marcado como no-show nao pode ser confirmado novamente
    Dado que existe um agendamento com status "Nao Compareceu" para "Joao Silva"
    Quando a recepcionista tenta confirmar novamente esse agendamento
    Entao o sistema deve bloquear a confirmacao
    E a mensagem de erro deve informar "Nao e permitido confirmar um agendamento marcado como nao compareceu"
