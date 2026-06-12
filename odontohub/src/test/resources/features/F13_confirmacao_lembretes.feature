# language: pt

Funcionalidade: Confirmacao e Lembretes de Consulta
  Como recepcionista
  Eu quero enviar lembretes e registrar a resposta do paciente antes da consulta
  Para reduzir faltas e manter a agenda confirmada com antecedencia

  Contexto:
    Dado que o dentista "Dr. Carlos" esta cadastrado no sistema
    E que o paciente "Joao Silva" esta cadastrado no sistema

  Cenario: Envio automatico de lembrete para consulta futura confirmada
    Dado que existe um agendamento com status "Confirmado" para "Joao Silva" em "15/05/2026 14:00"
    Quando o sistema envia o lembrete de consulta pelo canal "WhatsApp"
    Entao o lembrete deve ser registrado para o agendamento
    E o canal do ultimo lembrete deve ser "WhatsApp"
    E a resposta do paciente deve permanecer como "Pendente"

  Cenario: Paciente confirma presenca apos receber lembrete
    Dado que "Joao Silva" recebeu um lembrete para o agendamento de "15/05/2026 14:00"
    Quando o sistema registra a confirmacao de presenca de "Joao Silva"
    Entao a resposta do paciente deve ser "Confirmado"
    E a data da confirmacao do paciente deve ser registrada

  Cenario: Paciente recusa comparecimento apos receber lembrete
    Dado que "Joao Silva" recebeu um lembrete para o agendamento de "15/05/2026 14:00"
    Quando a recepcionista registra a recusa de "Joao Silva" com o motivo "Viagem"
    Entao a resposta do paciente deve ser "Recusado"
    E o agendamento deve ser cancelado com o motivo "Viagem"

  Cenario: Sistema nao envia lembrete para consulta cancelada
    Dado que existe um agendamento com status "Cancelado" para "Joao Silva" em "15/05/2026 14:00"
    Quando o sistema tenta enviar o lembrete de consulta
    Entao o sistema deve rejeitar o envio do lembrete
    E a mensagem de erro deve informar "Nao e permitido enviar lembrete para agendamento cancelado"

  Cenario: Sistema evita envio duplicado de lembrete no mesmo periodo configurado
    Dado que ja foi enviado um lembrete para "Joao Silva" nas ultimas 24 horas
    Quando o sistema tenta enviar um novo lembrete para o mesmo agendamento
    Entao o sistema deve impedir o envio duplicado
    E a mensagem de erro deve informar "Ja existe lembrete enviado para este agendamento no periodo configurado"
