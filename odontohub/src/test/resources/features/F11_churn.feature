# language: pt

Funcionalidade: Dashboard de Churn e Inteligência de Retenção
  Como Cirurgião-Dentista
  Eu quero visualizar a taxa de churn e os motivos de cancelamento
  Para identificar gargalos no atendimento e criar estratégias de recuperação de pacientes

  Contexto:
    Dado que o dentista possui valor médio por hora de R$300,00

  Cenário: Paciente é classificado automaticamente como Evadido
    Dado que o paciente "Marcos Vieira" não possui agendamentos futuros
    E que "Marcos Vieira" não visita a clínica há 13 meses
    E que "Marcos Vieira" possui um Plano de Tratamento com status "Em Andamento"
    Quando o sistema recalcula o status de churn dos pacientes
    Então "Marcos Vieira" deve ser classificado como "Evadido"

  Cenário: Paciente entra na Zona de Risco após 6 meses sem retorno
    Dado que o paciente "Renata Alves" não visita a clínica há 6 meses
    E que "Renata Alves" possui um Plano de Tratamento ativo
    Quando o sistema recalcula o status de churn dos pacientes
    Então "Renata Alves" deve ser classificada como "Zona de Risco"
    E deve ser gerado um alerta de inatividade progressiva para "Renata Alves"

  Cenário: Paciente evadido é reativado ao realizar novo agendamento
    Dado que o paciente "Marcos Vieira" está classificado como "Evadido"
    Quando "Marcos Vieira" realiza um novo agendamento na clínica
    Então o status de "Marcos Vieira" deve ser atualizado para "Reativado"

  Cenário: Cálculo de receita perdida por agenda ociosa
    Dado que o dentista teve 3 horas de agenda cancelada no mês
    Quando o sistema calcula a receita perdida por agenda ociosa
    Então o valor de receita perdida deve ser R$900,00

  Cenário: No-Show é diferenciado de Cancelamento Antecipado no dashboard
    Dado que existe um agendamento confirmado para "Marcos Vieira"
    Quando "Marcos Vieira" não comparece à consulta sem aviso prévio
    Então o agendamento deve ser registrado como "Não Compareceu"
    E este registro deve ser contabilizado separadamente dos cancelamentos antecipados no dashboard

  Cenário: Cancelamento de agendamento exige seleção de categoria de motivo
    Dado que existe um agendamento confirmado para "Renata Alves"
    Quando a recepcionista cancela o agendamento sem informar a categoria do motivo
    Então o sistema deve bloquear o cancelamento
    E a mensagem deve informar "A categoria do motivo de cancelamento é obrigatória"
