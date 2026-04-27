# language: pt
Funcionalidade: F01 - Agendamento de Consultas e Retornos
  Como recepcionista
  Eu quero registrar agendamentos de pacientes com data, horário e tipo de atendimento
  Para que a agenda do consultório seja organizada e sem conflitos

  Contexto:
    Dado que o dentista "Dr. Carlos" está cadastrado no sistema
    E que o paciente "João Silva" está cadastrado no sistema

  Cenário: Registrar consulta para paciente sem plano de tratamento ativo
    Dado que o paciente "João Silva" não possui plano de tratamento ativo
    Quando a recepcionista registra um agendamento para "João Silva" no dia "2026-05-10" às "09:00"
    Então o agendamento deve ser criado com status "PENDENTE"
    E o tipo do agendamento deve ser classificado automaticamente como "CONSULTA"

  Cenário: Registrar retorno para paciente com plano de tratamento ativo
    Dado que o paciente "João Silva" possui um plano de tratamento ativo
    Quando a recepcionista registra um agendamento para "João Silva" no dia "2026-05-10" às "09:00"
    Então o agendamento deve ser criado com status "PENDENTE"
    E o tipo do agendamento deve ser classificado automaticamente como "RETORNO"

  Cenário: Bloquear agendamento com conflito de horário
    Dado que já existe um agendamento para o dentista "Dr. Carlos" no dia "2026-05-10" às "09:00"
    Quando a recepcionista tenta registrar outro agendamento para "Dr. Carlos" no dia "2026-05-10" às "09:00"
    Então o sistema deve rejeitar o agendamento
    E exibir a mensagem "Já existe um agendamento para este horário"

  Cenário: Bloquear agendamento de paciente inadimplente
    Dado que o paciente "João Silva" possui uma parcela com status "INADIMPLENTE"
    Quando a recepcionista tenta registrar um agendamento para "João Silva"
    Então o sistema deve bloquear o registro do agendamento
    E exibir a mensagem "Paciente com inadimplência ativa"

  Cenário: Permitir agendamento de inadimplente com autorização do dentista
    Dado que o paciente "João Silva" possui uma parcela com status "INADIMPLENTE"
    E que o dentista "Dr. Carlos" autorizou o agendamento
    Quando a recepcionista registra um agendamento para "João Silva" com autorização do dentista
    Então o agendamento deve ser criado com status "PENDENTE"
    E o campo "autorizadoPorDentista" deve ser verdadeiro

  Cenário: Bloquear agendamento em data passada
    Quando a recepcionista tenta registrar um agendamento para o dia "2020-01-01"
    Então o sistema deve rejeitar o agendamento
    E exibir a mensagem "Não é permitido registrar agendamentos em datas passadas"

  Cenário: Confirmar agendamento pendente
    Dado que existe um agendamento com status "PENDENTE" para "João Silva"
    Quando a recepcionista confirma o agendamento
    Então o status do agendamento deve ser alterado para "CONFIRMADO"
    E o histórico de status deve registrar a alteração com data e responsável

  Cenário: Cancelar agendamento confirmado
    Dado que existe um agendamento com status "CONFIRMADO" para "João Silva"
    Quando a recepcionista cancela o agendamento com o motivo "Paciente não compareceu"
    Então o status do agendamento deve ser alterado para "CANCELADO"
    E o histórico de status deve registrar o motivo "Paciente não compareceu"
