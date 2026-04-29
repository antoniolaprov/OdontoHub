# language: pt

Funcionalidade: Agendamento de Consultas e Retornos
  Como recepcionista
  Eu quero registrar agendamentos de pacientes com data, horário e tipo de atendimento
  Para que a agenda do consultório seja organizada e sem conflitos

  Contexto:
    Dado que o dentista "Dr. Carlos" está cadastrado no sistema
    E que o paciente "João Silva" está cadastrado no sistema

  Cenário: Agendamento classificado como Consulta para paciente sem plano ativo
    Dado que o paciente "João Silva" não possui Plano de Tratamento ativo
    Quando a recepcionista registra um agendamento para "João Silva" com "Dr. Carlos" em "10/05/2026 09:00"
    Então o agendamento deve ser criado com o tipo "Consulta"
    E o status do agendamento deve ser "Agendado"

  Cenário: Agendamento classificado como Retorno para paciente com plano ativo
    Dado que o paciente "João Silva" possui um Plano de Tratamento com status "Em Andamento"
    Quando a recepcionista registra um agendamento para "João Silva" com "Dr. Carlos" em "10/05/2026 09:00"
    Então o agendamento deve ser criado com o tipo "Retorno"

  Cenário: Bloqueio de agendamento por conflito de horário do dentista
    Dado que já existe um agendamento confirmado para "Dr. Carlos" em "10/05/2026 09:00"
    Quando a recepcionista tenta registrar outro agendamento para "Dr. Carlos" em "10/05/2026 09:00"
    Então o sistema deve rejeitar o agendamento
    E a mensagem de erro deve informar "Conflito de horário: o dentista já possui um agendamento neste horário"

  Cenário: Bloqueio de agendamento para paciente inadimplente há mais de 30 dias
    Dado que o paciente "João Silva" possui parcelas vencidas há mais de 30 dias
    Quando a recepcionista tenta registrar um agendamento para "João Silva"
    Então o sistema deve rejeitar o agendamento
    E a mensagem de erro deve informar "Paciente restrito: possui parcelas em atraso há mais de 30 dias"

  Cenário: Rejeição de agendamento em data passada
    Quando a recepcionista tenta registrar um agendamento para "João Silva" com "Dr. Carlos" em "01/01/2020 09:00"
    Então o sistema deve rejeitar o agendamento
    E a mensagem de erro deve informar "Não é permitido registrar agendamentos em datas passadas"

  Cenário: Confirmação de agendamento registra responsável e data da alteração
    Dado que existe um agendamento com status "Agendado" para "João Silva"
    Quando a recepcionista "Ana" confirma o agendamento
    Então o status do agendamento deve ser "Confirmado"
    E o responsável pela alteração deve ser registrado como "Ana"
    E a data da última alteração deve ser registrada

  Cenário: Cancelamento de agendamento registra motivo obrigatório
    Dado que existe um agendamento com status "Confirmado" para "João Silva"
    Quando a recepcionista cancela o agendamento informando o motivo "Preço"
    Então o status do agendamento deve ser "Cancelado"
    E o motivo de cancelamento deve ser registrado como "Preço"

  Cenário: Remarcação de agendamento registra nova data e responsável
    Dado que existe um agendamento com status "Confirmado" para "João Silva" em "10/05/2026 09:00"
    Quando a recepcionista "Ana" remarca o agendamento para "12/05/2026 14:00"
    Então o status do agendamento deve ser "Remarcado"
    E a nova data deve ser "12/05/2026 14:00"
    E o responsável pela alteração deve ser registrado como "Ana"
