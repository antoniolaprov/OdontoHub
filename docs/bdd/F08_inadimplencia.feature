# language: pt
Funcionalidade: F08 - Controle de Inadimplência
  Como Cirurgião-Dentista
  Eu quero que o sistema identifique automaticamente parcelas vencidas e restrinja o agendamento de pacientes inadimplentes
  Para que o controle financeiro do consultório seja ativo e as perdas sejam minimizadas

  Contexto:
    Dado que o paciente "Fernanda Dias" está cadastrado no sistema
    E que existe um pagamento parcelado para "Fernanda Dias"

  Cenário: Identificar inadimplência automaticamente ao vencer parcela
    Dado que a parcela 1 do pagamento de "Fernanda Dias" tem vencimento em "2026-04-01" e status "PENDENTE"
    Quando o sistema processa as parcelas vencidas na data "2026-04-02"
    Então o status da parcela 1 deve ser alterado automaticamente para "INADIMPLENTE"
    E um registro de inadimplência deve ser criado para "Fernanda Dias"

  Cenário: Bloquear agendamento ao detectar inadimplência
    Dado que a parcela 1 de "Fernanda Dias" tem status "INADIMPLENTE"
    Quando a recepcionista tenta registrar um novo agendamento para "Fernanda Dias"
    Então o sistema deve bloquear o agendamento
    E exibir a mensagem "Paciente com inadimplência ativa. Regularize antes de agendar."

  Cenário: Remover bloqueio ao quitar parcela inadimplente
    Dado que a parcela 1 de "Fernanda Dias" tem status "INADIMPLENTE"
    E que "Fernanda Dias" não possui outras parcelas inadimplentes
    Quando a recepcionista registra a quitação da parcela 1 em "2026-05-10"
    Então o status da parcela 1 deve ser alterado para "LIQUIDADA"
    E o bloqueio de agendamento de "Fernanda Dias" deve ser removido automaticamente

  Cenário: Manter bloqueio ao quitar apenas uma das parcelas inadimplentes
    Dado que "Fernanda Dias" possui 2 parcelas com status "INADIMPLENTE"
    Quando a recepcionista quita apenas a parcela 1
    Então o bloqueio de agendamento de "Fernanda Dias" deve permanecer ativo
    E a mensagem deve informar que ainda existe inadimplência pendente

  Cenário: Registrar histórico de cobrança
    Dado que "Fernanda Dias" possui inadimplência ativa
    Quando a recepcionista registra uma tentativa de cobrança com resultado "Sem resposta"
    Então o histórico de cobrança deve registrar a data, o responsável e o resultado "Sem resposta"

  Cenário: Autorizar agendamento de inadimplente com registro do dentista
    Dado que "Fernanda Dias" possui inadimplência ativa
    Quando o dentista "Dr. Carlos" autoriza o agendamento de "Fernanda Dias" com justificativa
    Então o agendamento deve ser permitido
    E o nome do dentista autorizador deve ser registrado no agendamento
