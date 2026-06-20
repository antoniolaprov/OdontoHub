# language: pt

Funcionalidade: Agendamento de Consultas e Retornos
  Como recepcionista
  Eu quero registrar agendamentos de pacientes com data, horário e tipo de atendimento
  Para que a agenda do consultório seja organizada e sem conflitos

  Contexto:
    Dado que o dentista "Dra. Sofia Martins" está cadastrado no sistema
    E que o paciente "João Silva" está cadastrado no sistema

  Cenário: Agendamento classificado como Consulta para paciente sem plano ativo
    Dado que o paciente "João Silva" não possui Plano de Tratamento ativo
    Quando a recepcionista registra um agendamento para "João Silva" com "Dra. Sofia Martins" em "10/05/2030 09:00"
    Então o agendamento deve ser criado com o tipo "CONSULTA"
    E o status do agendamento deve ser "AGENDADO"

  Cenário: Agendamento classificado como Retorno para paciente com plano ativo
    Dado que o paciente "João Silva" possui um Plano de Tratamento ativo
    Quando a recepcionista registra um agendamento para "João Silva" com "Dra. Sofia Martins" em "10/05/2030 09:00"
    Então o agendamento deve ser criado com o tipo "RETORNO"

  Cenário: Bloqueio de agendamento por conflito de horário do dentista
    Dado que já existe um agendamento com status diferente de "CANCELADO" para "Dra. Sofia Martins" em "10/05/2030 09:00"
    Quando a recepcionista tenta registrar outro agendamento para "Dra. Sofia Martins" em "10/05/2030 09:00"
    Então o sistema deve rejeitar o agendamento
    E a mensagem de erro deve informar "Conflito de horário: Dra. Sofia Martins já possui agendamento às 09:00 neste dia."

  Cenário: Bloqueio de agendamento para paciente inadimplente
    Dado que o paciente "João Silva" possui a flag "inadimplente" como verdadeira
    Quando a recepcionista tenta registrar um agendamento para "João Silva"
    Então o sistema deve exibir o alerta visual "🚫 Paciente Inadimplente"
    E o agendamento não deve ser criado

  Cenário: Rejeição de agendamento em data passada
    Quando a recepcionista tenta registrar um agendamento para "João Silva" com "Dra. Sofia Martins" em "01/01/2020 09:00"
    Então o sistema deve rejeitar o agendamento
    E a mensagem de erro deve informar "Não é possível agendar para datas passadas."

  Cenário: Confirmação de agendamento atualiza status e registra no histórico
    Dado que existe um agendamento com status "AGENDADO" para "João Silva"
    Quando a recepcionista confirma o agendamento
    Então o status do agendamento deve ser "CONFIRMADO"
    E o histórico deve conter uma entrada com a ação "Confirmado"
    E o responsável registrado no histórico deve ser "Recepcionista"
    E a data da última alteração deve ser atualizada para hoje

  Cenário: Cancelamento de agendamento exige motivo obrigatório
    Dado que existe um agendamento com status diferente de "CANCELADO" para "João Silva"
    Quando a recepcionista tenta cancelar o agendamento sem informar motivo
    Então o sistema deve rejeitar o cancelamento
    E a mensagem de erro deve informar "Informe o motivo do cancelamento."

  Cenário: Cancelamento de agendamento registra motivo no histórico
    Dado que existe um agendamento com status diferente de "CANCELADO" para "João Silva"
    Quando a recepcionista cancela o agendamento informando o motivo "Preço"
    Então o status do agendamento deve ser "CANCELADO"
    E o campo "motivoCancelamento" deve ser registrado como "Preço"
    E o histórico deve conter uma entrada com a ação "Cancelado: Preço"

  Cenário: Remarcação de agendamento registra nova data e atualiza status
    Dado que existe um agendamento com status diferente de "CANCELADO" para "João Silva" em "10/05/2030 09:00"
    Quando a recepcionista remarca o agendamento para "13/05/2030 14:00"
    Então o status do agendamento deve ser "REMARCADO"
    E a nova data do agendamento deve ser "13/05/2030"
    E o novo horário deve ser "14:00"
    E o histórico deve conter uma entrada com a ação "Remarcado para 13/05/2030 às 14:00"
    E o responsável registrado no histórico deve ser "Recepcionista"

  Cenário: Rejeição de remarcação em data passada
    Dado que existe um agendamento com status diferente de "CANCELADO" para "João Silva"
    Quando a recepcionista tenta remarcar o agendamento para "01/01/2020 09:00"
    Então o sistema deve rejeitar a remarcação
    E a mensagem de erro deve informar "Não é possível remarcar para datas passadas."
