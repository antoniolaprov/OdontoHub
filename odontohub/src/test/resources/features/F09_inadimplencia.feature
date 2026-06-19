# language: pt

Funcionalidade: Gestão Ativa de Inadimplência e Acordos
  Como Recepcionista
  Eu quero visualizar parcelas vencidas, acompanhar pacientes inadimplentes e registrar acordos de pagamento
  Para que a clínica consiga organizar cobranças e negociar débitos de forma controlada

  Contexto:
    Dado que o paciente "João Silva" possui parcelas registradas no sistema

  Cenário: Identificação automática de paciente inadimplente
    Dado que "João Silva" possui uma parcela vencida há 10 dias no valor de R$ 500,00
    Quando a recepcionista consulta a lista de inadimplentes
    Então o sistema deve exibir a parcela vencida
    E deve exibir o valor original da parcela
    E deve exibir os dias de atraso
    E deve exibir os juros calculados automaticamente
    E deve exibir a multa calculada automaticamente
    E deve exibir o valor atualizado da dívida

  Cenário: Aplicação automática do status Restrito após 30 dias de atraso
    Dado que "João Silva" possui uma parcela vencida há 31 dias
    Quando o sistema atualizar a situação financeira do paciente
    Então o paciente deve receber o status "Restrito"
    E um alerta visual deve ser exibido em seu cadastro

  Cenário: Remoção automática do status Restrito após quitação das pendências
    Dado que "João Silva" possui status "Restrito"
    E não possui parcelas vencidas
    E não possui acordos inadimplidos
    Quando a última pendência financeira for quitada
    Então o status "Restrito" deve ser removido automaticamente

  Cenário: Bloqueio de agendamento para paciente Restrito
    Dado que "João Silva" possui status "Restrito"
    Quando a recepcionista tenta registrar um novo agendamento não emergencial
    Então o sistema deve bloquear o agendamento
    E a mensagem deve informar "Paciente possui restrição financeira"

  Cenário: Registro de tentativa de cobrança
    Dado que "João Silva" possui parcelas vencidas
    Quando a recepcionista registra uma tentativa de cobrança via "Telefone"
    Então o sistema deve armazenar a data do contato
    E deve armazenar o responsável pelo contato
    E deve armazenar o canal utilizado
    E deve armazenar o resultado do contato
    E deve armazenar a observação informada

  Cenário: Destaque visual para inadimplente sem cobrança recente
    Dado que "João Silva" possui parcelas vencidas
    E não possui tentativa de cobrança registrada nos últimos 15 dias
    Quando a recepcionista consulta a lista de inadimplentes
    Então o paciente deve ser exibido com destaque visual

  Cenário: Criação de acordo de pagamento
    Dado que "João Silva" possui duas parcelas vencidas
    Quando a recepcionista cria um acordo com 4 parcelas e justificativa "Renegociação solicitada pelo paciente"
    Então as parcelas originais devem receber o status "Substituída"
    E um novo acordo deve ser criado com status "Ativo"
    E novas parcelas devem ser geradas conforme as condições negociadas

  Cenário: Encargos são incorporados automaticamente ao acordo
    Dado que "João Silva" possui parcelas vencidas com juros e multa acumulados
    Quando a recepcionista cria um acordo de pagamento
    Então o valor total do acordo deve incluir todos os encargos calculados até a data da negociação
    E a recepcionista não deve poder editar juros ou multa manualmente

  Cenário: Impedir criação de acordo sem parcelas vencidas
    Dado que "João Silva" não possui parcelas vencidas
    Quando a recepcionista tenta criar um acordo
    Então o sistema deve bloquear a operação
    E a mensagem deve informar "É necessário possuir ao menos uma parcela vencida"

  Cenário: Impedir criação de novo acordo quando já existe acordo ativo
    Dado que "João Silva" possui um acordo com status "Ativo"
    Quando a recepcionista tenta criar um novo acordo
    Então o sistema deve bloquear a operação
    E a mensagem deve informar "Já existe um acordo ativo para este paciente"

  Cenário: Marcação automática de acordo inadimplido
    Dado que "João Silva" possui um acordo com status "Ativo"
    E uma parcela do acordo encontra-se vencida
    Quando o sistema atualizar a situação financeira do paciente
    Então o acordo deve receber o status "Inadimplido"
    E o paciente deve receber o status "Restrito"

  Cenário: Cancelamento de acordo
    Dado que "João Silva" possui um acordo com status "Ativo"
    Quando a recepcionista cancela o acordo com justificativa "Solicitação administrativa"
    Então o acordo deve receber o status "Cancelado"
    E as parcelas geradas pelo acordo devem receber o status "Cancelada"
    E as parcelas originais devem retornar ao status "Vencida"
    E os encargos devem ser recalculados para a data do cancelamento

  Cenário: Registro de histórico de negociação
    Dado que "João Silva" possui um acordo com status "Ativo"
    Quando a recepcionista altera o status do acordo
    Então o histórico deve registrar o responsável
    E deve registrar a data da alteração
    E deve registrar o status anterior
    E deve registrar o novo status
    E deve registrar a justificativa informada

  Cenário: Encaminhamento para registro de pagamento
    Dado que "João Silva" possui uma parcela vencida
    Quando a recepcionista seleciona a opção registrar pagamento
    Então o sistema deve direcionar para a funcionalidade de pagamentos
    E a parcela selecionada deve ser enviada automaticamente