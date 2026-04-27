# language: pt

Funcionalidade: Fluxo de Caixa do Consultório
  Como Cirurgião-Dentista
  Eu quero visualizar todas as entradas e saídas financeiras e projetar o saldo futuro
  Para tomar decisões financeiras fundamentadas

  Cenário: Geração automática de entrada ao liquidar uma parcela
    Dado que existe uma parcela de R$500,00 com status "Pendente"
    Quando a parcela é liquidada
    Então deve ser gerado automaticamente um lançamento de entrada no Fluxo de Caixa de R$500,00
    E o lançamento deve ser marcado como gerado automaticamente
    E não deve ser possível editar esse lançamento manualmente

  Cenário: Registro manual de saída avulsa com categoria obrigatória
    Quando o dentista registra uma saída manual de R$200,00 na categoria "Material de escritório" com descrição "Compra de papel"
    Então o lançamento de saída deve ser registrado no Fluxo de Caixa
    E o lançamento deve permitir edição futura

  Cenário: Alerta de fluxo negativo quando saídas projetadas superam entradas
    Dado que as entradas confirmadas do mês somam R$3.000,00
    E que as saídas previstas do mês somam R$4.000,00
    Quando o saldo projetado é calculado
    Então o sistema deve emitir um alerta de fluxo negativo
    E o saldo projetado deve ser de R$-1.000,00

  Cenário: Cálculo do ponto de equilíbrio do consultório
    Dado que os custos fixos mensais do consultório são de R$5.000,00
    E que o valor médio por procedimento é de R$250,00
    Quando o ponto de equilíbrio é calculado
    Então o sistema deve informar que são necessários 20 procedimentos para cobrir os custos fixos

  Cenário: Reposição de estoque gera saída automática no Fluxo de Caixa
    Dado que o material "Luva cirúrgica" é reposto com 50 unidades a R$1,50 cada
    Quando a reposição é registrada
    Então deve ser gerado automaticamente um lançamento de saída de R$75,00 na categoria "Insumos"
    E o lançamento deve ser marcado como gerado automaticamente
