# language: pt
Funcionalidade: F07 - Registro de Pagamento com Parcelamento
  Como recepcionista
  Eu quero registrar o pagamento do paciente referente ao orçamento aprovado
  Para que o controle financeiro do consultório seja preciso e atualizado

  Contexto:
    Dado que o paciente "Roberto Costa" está cadastrado no sistema
    E que existe um orçamento aprovado para "Roberto Costa" no valor de R$ 600,00

  Cenário: Registrar pagamento à vista
    Quando a recepcionista registra um pagamento à vista de R$ 600,00 para o orçamento de "Roberto Costa"
    Então o pagamento deve ser criado com tipo "A_VISTA"
    E deve conter uma única parcela com status "LIQUIDADA"

  Cenário: Registrar pagamento parcelado com valor total correto
    Quando a recepcionista registra um pagamento parcelado em 3 vezes de R$ 200,00
    Então o pagamento deve ser criado com tipo "PARCELADO"
    E devem existir 3 parcelas com status "PENDENTE" e valor R$ 200,00 cada

  Cenário: Bloquear pagamento com valor total divergente do orçamento
    Quando a recepcionista tenta registrar um pagamento parcelado em 2 vezes de R$ 250,00
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "O valor total das parcelas deve corresponder ao valor do orçamento aprovado"

  Cenário: Bloquear pagamento sem vínculo com orçamento aprovado
    Quando a recepcionista tenta registrar um pagamento sem informar o orçamento
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "Todo pagamento deve estar vinculado a um orçamento aprovado"

  Cenário: Liquidar parcela futura antecipadamente
    Dado que existe um pagamento parcelado com a parcela 2 com vencimento em "2026-07-10" e status "PENDENTE"
    Quando a recepcionista liquida a parcela 2 em "2026-05-20"
    Então o status da parcela 2 deve ser alterado para "LIQUIDADA"
    E a data de pagamento efetivo deve ser registrada como "2026-05-20"
    E o status das demais parcelas não deve ser alterado

  Cenário: Manter status independente por parcela
    Dado que existe um pagamento com 3 parcelas todas com status "PENDENTE"
    Quando a recepcionista liquida apenas a parcela 1
    Então a parcela 1 deve ter status "LIQUIDADA"
    E as parcelas 2 e 3 devem permanecer com status "PENDENTE"

  Cenário: Bloquear exclusão de pagamento com parcelas liquidadas
    Dado que existe um pagamento com a parcela 1 com status "LIQUIDADA"
    Quando qualquer usuário tenta excluir o pagamento
    Então o sistema deve rejeitar a exclusão
    E exibir a mensagem "Pagamentos com parcelas liquidadas não podem ser excluídos"