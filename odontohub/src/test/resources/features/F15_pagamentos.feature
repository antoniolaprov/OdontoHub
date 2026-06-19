# language: pt

Funcionalidade: Gestão de Pagamentos, Quitação de Débitos e Portal do Paciente
  Como recepcionista ou paciente
  Eu quero registrar e confirmar pagamentos de parcelas e acordos
  Para que o fluxo de caixa seja atualizado corretamente e comprovantes sejam emitidos

  Contexto:
    Dado que o paciente "Roberto Lima" está cadastrado no sistema
    E que a recepcionista "Ana Paula" está autenticada no sistema
    E que existe uma parcela em aberto no valor de "R$ 300,00" vinculada ao tratamento de "Roberto Lima"


  Cenário: Registro de pagamento total quita a parcela e gera entrada no fluxo de caixa
    Quando a recepcionista registra o pagamento da parcela de "Roberto Lima" com os dados:
      | campo           | valor      |
      | valorRecebido   | 300,00     |
      | dataPagamento   | hoje       |
      | formaPagamento  | PIX        |
    Então a parcela deve ser marcada como "Paga"
    E uma entrada de "R$ 300,00" deve ser gerada automaticamente no Fluxo de Caixa

  Cenário: Registro de pagamento parcial mantém saldo em aberto
    Quando a recepcionista registra o pagamento da parcela de "Roberto Lima" com os dados:
      | campo           | valor      |
      | valorRecebido   | 150,00     |
      | dataPagamento   | hoje       |
      | formaPagamento  | Dinheiro   |
    Então a parcela deve ser marcada como "Parcialmente Paga"
    E o saldo restante da parcela deve ser "R$ 150,00"
    E uma entrada de "R$ 150,00" deve ser gerada no Fluxo de Caixa

  Cenário: Pagamento que quita todas as parcelas vencidas remove o status Restrito do paciente
    Dado que o paciente "Roberto Lima" possui status "Restrito" por inadimplência
    E que a parcela em aberto é a única parcela vencida de "Roberto Lima"
    Quando a recepcionista registra o pagamento total da parcela de "Roberto Lima"
    Então o status de inadimplência de "Roberto Lima" deve ser removido
    E o paciente não deve mais possuir o status "Restrito"

  Cenário: Rejeição de pagamento com data futura
    Quando a recepcionista tenta registrar um pagamento para "Roberto Lima" com data "31/12/2099"
    Então o sistema deve rejeitar o pagamento
    E a mensagem de erro deve informar "A data do pagamento não pode ser futura."

  Cenário: Rejeição de pagamento com valor zero ou negativo
    Quando a recepcionista tenta registrar um pagamento para "Roberto Lima" com valor "-50,00"
    Então o sistema deve rejeitar o pagamento
    E a mensagem de erro deve informar "O valor recebido deve ser maior que zero."

  Cenário: Registro de pagamento com observação opcional
    Quando a recepcionista registra o pagamento da parcela de "Roberto Lima" informando a observação "Paciente solicitou parcelamento diferenciado"
    Então o pagamento deve ser salvo com a observação "Paciente solicitou parcelamento diferenciado"

  Cenário: Pagamento de parcela gerada por acordo é registrado normalmente
    Dado que existe uma parcela de acordo na F9 em aberto no valor de "R$ 200,00" para "Roberto Lima"
    Quando a recepcionista registra o pagamento total desta parcela com forma de pagamento "Cartão de Débito"
    Então a parcela de acordo deve ser marcada como "Paga"
    E uma entrada de "R$ 200,00" deve ser gerada no Fluxo de Caixa


  Cenário: Paciente com status Restrito acessa o portal e visualiza aviso de restrição
    Dado que o paciente "Roberto Lima" possui status "Restrito"
    Quando "Roberto Lima" acessa o portal de pagamento
    Então o portal deve exibir o aviso "Sua conta está restrita. Quite as pendências abaixo para liberar o agendamento automaticamente."
    E as parcelas em aberto devem ser exibidas normalmente

  Cenário: Portal exibe informações completas de cada parcela
    Quando "Roberto Lima" acessa o portal de pagamento
    Então cada parcela listada deve exibir: valor original, valor atualizado, vencimento, status, juros, multa e tratamento relacionado

  Cenário: Pagamento de múltiplas parcelas gera cobrança unificada
    Dado que "Roberto Lima" possui duas parcelas em aberto de "R$ 150,00" cada
    Quando "Roberto Lima" seleciona as duas parcelas no portal e escolhe pagar por "PIX"
    Então o sistema deve gerar uma cobrança unificada de "R$ 300,00"

  Cenário: Cobrança unificada não confirmada mantém todas as parcelas em aberto
    Dado que "Roberto Lima" iniciou um pagamento unificado de duas parcelas por PIX
    Quando o pagamento não é confirmado dentro do prazo
    Então ambas as parcelas devem permanecer com status "Em Aberto"

  Cenário: Geração de código PIX no portal exibe prazo de validade
    Quando "Roberto Lima" seleciona uma parcela e escolhe pagar por "PIX"
    Então o sistema deve gerar um código PIX para pagamento
    E deve exibir o prazo de validade da cobrança

  Cenário: Código PIX expirado cancela a tentativa e libera a parcela para nova tentativa
    Dado que "Roberto Lima" gerou um código PIX para pagamento de uma parcela
    Quando o prazo de validade do PIX expira sem confirmação
    Então o registro de tentativa pendente deve ser cancelado automaticamente
    E a parcela deve permanecer com status "Em Aberto"
    E a parcela deve estar disponível para nova tentativa de pagamento

  Cenário: Confirmação de pagamento online marca parcela como Paga e atualiza fluxo de caixa
    Quando o pagamento online de "Roberto Lima" é confirmado pelo valor total da parcela
    Então a parcela deve ser marcada como "Paga"
    E uma entrada deve ser gerada automaticamente no Fluxo de Caixa

  Cenário: Parcelas com status inválido não podem ser selecionadas no portal
    Dado que "Roberto Lima" possui uma parcela com status "Cancelada"
    Quando "Roberto Lima" acessa o portal de pagamento
    Então a parcela com status "Cancelada" não deve estar disponível para seleção

  Cenário: Comprovante é disponibilizado ao paciente após confirmação do pagamento online
    Quando o pagamento online de "Roberto Lima" é confirmado
    Então o sistema deve disponibilizar um comprovante contendo: dados da clínica, nome do paciente, valor pago, forma de pagamento, data e identificador único do pagamento

  Cenário: Pagamento confirmado no portal atualiza automaticamente a F9 quando quita parcela de acordo
    Dado que "Roberto Lima" possui uma parcela de acordo em aberto no portal
    Quando "Roberto Lima" realiza o pagamento total desta parcela pelo portal
    Então a F9 deve ser atualizada automaticamente refletindo a quitação da parcela do acordo

  Cenário: Recepcionista marca parcela como Aguardando Comprovante
    Quando a recepcionista marca a parcela de "Roberto Lima" como "Aguardando Comprovante"
    Então o status da parcela deve ser "Aguardando Comprovante"
    E nenhuma entrada deve ser gerada no Fluxo de Caixa

  Cenário: Bloqueio de segundo registro Aguardando Comprovante para a mesma parcela
    Dado que já existe um registro "Aguardando Comprovante" para a parcela de "Roberto Lima"
    Quando a recepcionista tenta marcar a mesma parcela novamente como "Aguardando Comprovante"
    Então o sistema deve rejeitar a ação
    E a mensagem de erro deve informar "Já existe um comprovante aguardando confirmação para esta parcela. Cancele ou confirme o registro existente antes de criar um novo."

  Cenário: Confirmação manual do comprovante baixa a parcela e gera entrada no fluxo de caixa
    Dado que a parcela de "Roberto Lima" está com status "Aguardando Comprovante"
    Quando a recepcionista confirma o recebimento informando valor "300,00" e data "hoje"
    Então a parcela deve ser marcada como "Paga"
    E uma entrada de "R$ 300,00" deve ser gerada no Fluxo de Caixa

  Cenário: Cancelamento de pagamento pendente exige justificativa obrigatória
    Dado que a parcela de "Roberto Lima" está com status "Aguardando Comprovante"
    Quando a recepcionista tenta cancelar o lançamento pendente sem informar justificativa
    Então o sistema deve rejeitar o cancelamento
    E a mensagem de erro deve informar "Informe a justificativa para cancelar o pagamento pendente."

  Cenário: Alerta visual para comprovantes pendentes há mais de dois dias úteis
    Dado que a parcela de "Roberto Lima" está com status "Aguardando Comprovante" há mais de dois dias úteis
    Quando a recepcionista acessa a tela de pagamentos
    Então o sistema deve exibir um alerta visual indicando a pendência de comprovante de "Roberto Lima"


  Cenário: Histórico exibe pagamentos confirmados, pendentes e cancelados
    Dado que "Roberto Lima" possui um pagamento confirmado, um pendente e um cancelado
    Quando a recepcionista consulta o histórico financeiro de "Roberto Lima"
    Então os três registros devem ser exibidos com seus respectivos status

  Cenário: Cada registro do histórico exibe todos os dados obrigatórios
    Quando a recepcionista consulta o histórico financeiro de "Roberto Lima"
    Então cada registro deve exibir: valor, data, forma de pagamento, parcela vinculada, status e responsável pelo registro

  Cenário: Geração de comprovante para pagamento confirmado
    Dado que "Roberto Lima" possui um pagamento com status "Confirmado"
    Quando a recepcionista solicita o comprovante deste pagamento
    Então o sistema deve gerar o comprovante com os dados do pagamento

  Cenário: Declaração de quitação é habilitada somente quando todas as parcelas do escopo estão pagas
    Dado que o tratamento de "Roberto Lima" possui 3 parcelas e todas estão com status "Paga"
    Quando a recepcionista acessa a opção de declaração de quitação pelo escopo de tratamento
    Então o sistema deve habilitar a geração da declaração de quitação total

  Cenário: Declaração de quitação é bloqueada quando existe parcela em aberto no escopo
    Dado que o tratamento de "Roberto Lima" possui 3 parcelas e 1 delas está com status "Em Aberto"
    Quando a recepcionista acessa a opção de declaração de quitação pelo escopo de tratamento
    Então o botão de geração da declaração deve estar indisponível
    E o sistema deve informar "Existem parcelas em aberto neste escopo. Quite todas as parcelas para emitir a declaração."

  Cenário: Declaração de quitação por escopo de acordo valida apenas as parcelas do acordo
    Dado que "Roberto Lima" possui um acordo na F9 com 2 parcelas e ambas estão com status "Paga"
    Quando a recepcionista solicita a declaração de quitação pelo escopo de acordo
    Então o sistema deve habilitar e gerar a declaração referente ao acordo quitado