# language: pt

Funcionalidade: Gestão de Pagamentos e Quitação de Débitos
  Como recepcionista
  Eu quero registrar pagamentos de parcelas presenciais e remotos
  Para que a baixa da dívida seja feita com dados reais e o fluxo de caixa seja atualizado automaticamente

  Contexto:
    Dado que existe uma parcela "P1" no valor de R$500,00 a quitar

  Cenário: Pagamento presencial integral quita a parcela e alimenta o fluxo de caixa
    Quando a recepcionista registra o pagamento presencial de R$500,00 na forma "PIX" para a parcela "P1"
    Então a parcela "P1" deve ficar com status "PAGA"
    E deve ser gerada uma entrada no fluxo de caixa de R$500,00

  Cenário: Pagamento parcial deixa saldo remanescente em aberto
    Quando a recepcionista registra o pagamento presencial de R$300,00 na forma "DINHEIRO" para a parcela "P1"
    Então a parcela "P1" deve ficar com status "PARCIALMENTE_PAGA"
    E o saldo remanescente da parcela "P1" deve ser R$200,00

  Cenário: Pagamento com data futura é rejeitado
    Quando a recepcionista tenta registrar um pagamento com data futura para a parcela "P1"
    Então o sistema deve rejeitar o pagamento
    E a mensagem de erro deve informar "A data do pagamento não pode ser futura"

  Cenário: Pagamento com valor zero é rejeitado
    Quando a recepcionista tenta registrar um pagamento de R$0,00 para a parcela "P1"
    Então o sistema deve rejeitar o pagamento
    E a mensagem de erro deve informar "O valor do pagamento deve ser positivo"

  Cenário: Lançamento aguardando comprovante não baixa a parcela nem gera fluxo de caixa
    Quando a recepcionista lança um pagamento aguardando comprovante na forma "TRANSFERENCIA" para a parcela "P1"
    Então a parcela "P1" deve ficar com status "EM_ABERTO"
    E nenhuma entrada deve ser gerada no fluxo de caixa

  Cenário: Confirmação do lançamento aguardando baixa a parcela
    Dado que existe um lançamento aguardando comprovante na forma "PIX" para a parcela "P1"
    Quando a recepcionista confirma o pagamento de R$500,00 na data de hoje para a parcela "P1"
    Então a parcela "P1" deve ficar com status "PAGA"

  Cenário: Bloqueio de dois lançamentos aguardando para a mesma parcela
    Dado que existe um lançamento aguardando comprovante na forma "PIX" para a parcela "P1"
    Quando a recepcionista tenta lançar outro pagamento aguardando comprovante para a parcela "P1"
    Então o sistema deve rejeitar o pagamento
    E a mensagem de erro deve informar "Já existe um lançamento aguardando comprovante para esta parcela"

  Cenário: Comprovante só é gerado para pagamento confirmado
    Quando a recepcionista registra o pagamento presencial de R$500,00 na forma "PIX" para a parcela "P1"
    Então deve ser possível gerar o comprovante do pagamento da parcela "P1"

  Cenário: Declaração de quitação é habilitada quando todas as parcelas estão pagas
    Quando a recepcionista registra o pagamento presencial de R$500,00 na forma "PIX" para a parcela "P1"
    Então a declaração de quitação total deve estar disponível
