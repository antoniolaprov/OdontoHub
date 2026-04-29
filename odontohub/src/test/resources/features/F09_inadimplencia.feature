# language: pt

Funcionalidade: Gestão Ativa de Inadimplência e Acordos
  Como Recepcionista
  Eu quero identificar parcelas vencidas e registrar acordos de pagamento
  Para recuperar crédito sem depender de cálculos manuais de juros

  Contexto:
    Dado que o paciente "Fernando Costa" está cadastrado no sistema

  Cenário: Paciente recebe status Restrito após 30 dias de atraso
    Dado que "Fernando Costa" possui uma parcela vencida há 31 dias
    Quando o sistema verifica o status de inadimplência
    Então "Fernando Costa" deve receber o status de "Restrito"
    E novos agendamentos não emergenciais devem ser bloqueados para "Fernando Costa"

  Cenário: Paciente com menos de 30 dias de atraso não recebe status Restrito
    Dado que "Fernando Costa" possui uma parcela vencida há 15 dias
    Quando o sistema verifica o status de inadimplência
    Então "Fernando Costa" não deve receber o status de "Restrito"

  Cenário: Cálculo automático de juros e multa sem permissão de edição manual
    Dado que "Fernando Costa" possui uma parcela de R$400,00 vencida há 15 dias
    Quando a recepcionista consulta os valores atualizados da parcela
    Então os juros e a multa devem ser calculados automaticamente pelo sistema
    E a recepcionista não deve conseguir alterar os valores de juros e multa

  Cenário: Consolidação de acordo substitui parcelas originais por novas
    Dado que "Fernando Costa" possui 3 parcelas vencidas no valor total de R$900,00
    Quando a recepcionista firma um acordo consolidando as 3 parcelas em 2 novas parcelas de R$450,00 cada
    Então as 3 parcelas originais devem ter o status "Substituídas"
    E 2 novas parcelas devem ser geradas com status "Pendente"
    E as parcelas substituídas não devem somar no Fluxo de Caixa

  Cenário: Multas retroativas voltam a valer ao inadimplir o acordo
    Dado que "Fernando Costa" firmou um acordo com parcelas substituídas
    Quando "Fernando Costa" deixa de pagar as novas parcelas do acordo
    Então o acordo deve ser marcado como inadimplido
    E as multas retroativas das parcelas originais devem voltar a ser consideradas
