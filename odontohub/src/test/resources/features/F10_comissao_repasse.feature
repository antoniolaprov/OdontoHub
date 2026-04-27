# language: pt
Funcionalidade: F10 - Cálculo de Comissão e Repasse de Especialista
  Como Cirurgião-Dentista
  Eu quero que o sistema calcule automaticamente a comissão do especialista ao registrar um procedimento realizado por ele
  Para que o repasse financeiro seja preciso e rastreável

  Contexto:
    Dado que o especialista "Dr. Marcos" está cadastrado no sistema
    E que a regra de comissão de "Dr. Marcos" para "Implante" é de 30% do valor do procedimento
    E que existe um plano de tratamento aprovado com o procedimento "Implante" no valor de R$ 2000,00

  Cenário: Calcular comissão automaticamente ao registrar procedimento realizado por especialista
    Quando o dentista registra o procedimento "Implante" como realizado por "Dr. Marcos"
    Então o sistema deve calcular automaticamente a comissão de R$ 600,00
    E a comissão deve ter status "PENDENTE"
    E deve estar vinculada a "Dr. Marcos" e ao procedimento "Implante"

  Cenário: Calcular comissão por valor fixo
    Dado que a regra de comissão de "Dr. Marcos" para "Extração" é de valor fixo R$ 100,00
    Quando o dentista registra o procedimento "Extração" como realizado por "Dr. Marcos"
    Então o sistema deve calcular a comissão como R$ 100,00 independente do valor do procedimento

  Cenário: Bloquear repasse antes do pagamento do paciente
    Dado que a comissão de "Dr. Marcos" pelo procedimento "Implante" tem status "PENDENTE"
    E que o paciente ainda não liquidou o pagamento correspondente
    Quando a recepcionista tenta registrar o repasse para "Dr. Marcos"
    Então o sistema deve bloquear o registro
    E exibir a mensagem "O repasse só pode ser registrado após o pagamento do paciente"

  Cenário: Liberar repasse após confirmação do pagamento do paciente
    Dado que a comissão de "Dr. Marcos" tem status "PENDENTE"
    Quando o paciente liquida o pagamento referente ao procedimento "Implante"
    Então a comissão deve ter status alterado para "LIBERADA"
    E o repasse pode ser registrado

  Cenário: Registrar repasse e lançar no fluxo de caixa
    Dado que a comissão de "Dr. Marcos" tem status "LIBERADA"
    Quando a recepcionista registra o repasse de R$ 600,00 para "Dr. Marcos" em "2026-05-15"
    Então o repasse deve ser criado com valor R$ 600,00 e data "2026-05-15"
    E um lançamento de saída de R$ 600,00 deve ser criado automaticamente no fluxo de caixa

  Cenário: Impedir exclusão de repasse registrado
    Dado que existe um repasse registrado para "Dr. Marcos"
    Quando qualquer usuário tenta excluir o repasse
    Então o sistema deve rejeitar a exclusão
    E exibir a mensagem "Repasses registrados não podem ser excluídos, apenas estornados"

  Cenário: Estornar repasse com justificativa
    Dado que existe um repasse registrado para "Dr. Marcos"
    Quando o dentista estorna o repasse com a justificativa "Procedimento refeito sem custo"
    Então o repasse deve ter o campo "estornado" marcado como verdadeiro
    E a justificativa de estorno "Procedimento refeito sem custo" deve ser salva no repasse
