# language: pt
Funcionalidade: F09 - Fluxo de Caixa do Consultório
  Como Cirurgião-Dentista
  Eu quero visualizar todas as entradas e saídas financeiras do consultório e projetar o saldo futuro
  Para que eu possa tomar decisões financeiras fundamentadas

  Contexto:
    Dado que o sistema possui lançamentos financeiros registrados

  Cenário: Gerar entrada automática ao liquidar parcela
    Dado que a parcela 1 do paciente "João Silva" no valor de R$ 200,00 está com status "PENDENTE"
    Quando a recepcionista liquida a parcela 1
    Então um lançamento de entrada de R$ 200,00 deve ser criado automaticamente no fluxo de caixa
    E o lançamento deve ter o campo "geradoAutomaticamente" como verdadeiro
    E o lançamento deve referenciar a parcela liquidada como origem

  Cenário: Impedir edição de entrada gerada automaticamente
    Dado que existe um lançamento de entrada gerado automaticamente por liquidação de parcela
    Quando qualquer usuário tenta editar o valor do lançamento
    Então o sistema deve rejeitar a edição
    E exibir a mensagem "Entradas geradas automaticamente não podem ser editadas"

  Cenário: Registrar saída avulsa com dados obrigatórios
    Quando o dentista registra uma saída de R$ 500,00 na categoria "Aluguel" com a justificativa "Aluguel de maio"
    Então o lançamento de saída deve ser criado com valor R$ 500,00
    E a categoria "Aluguel" e a justificativa devem ser registradas

  Cenário: Bloquear saída avulsa sem justificativa
    Quando o dentista tenta registrar uma saída sem informar a justificativa
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "A justificativa é obrigatória para saídas avulsas"

  Cenário: Calcular saldo projetado com parcelas a vencer
    Dado que existem as seguintes parcelas a vencer:
      | Parcela | Vencimento | Valor  |
      | 1       | 2026-06-01 | 300,00 |
      | 2       | 2026-07-01 | 300,00 |
    E existe uma saída prevista de R$ 200,00
    Quando o dentista solicita a projeção de saldo até "2026-07-31"
    Então o saldo projetado deve considerar entradas de R$ 600,00 e saídas de R$ 200,00
    E o saldo projetado deve ser R$ 400,00

  Cenário: Registrar saída de reposição de estoque automaticamente
    Dado que uma reposição de "Luvas" com quantidade 100 e custo unitário R$ 0,50 foi registrada
    Então um lançamento de saída de R$ 50,00 deve ser criado automaticamente na categoria "Insumos"
