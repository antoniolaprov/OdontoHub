# language: pt
Funcionalidade: F06 - Geração e Aprovação de Orçamento
  Como Cirurgião-Dentista
  Eu quero que o sistema gere automaticamente um orçamento a partir do plano de tratamento
  Para que o paciente tenha clareza dos custos antes do início dos procedimentos

  Contexto:
    Dado que o paciente "Lucia Ramos" está cadastrado no sistema
    E que existe um plano de tratamento ativo para "Lucia Ramos" com os procedimentos:
      | Procedimento | Valor Unitário |
      | Extração     | 250.00         |
      | Limpeza      | 150.00         |

  Cenário: Gerar orçamento automaticamente ao finalizar plano de tratamento
    Quando o dentista finaliza o plano de tratamento de "Lucia Ramos"
    Então o sistema deve gerar automaticamente um orçamento
    E o valor total do orçamento deve ser R$ 400,00
    E o orçamento deve ter status "PENDENTE"
    E deve conter os itens "Extração" e "Limpeza" com seus valores unitários

  Cenário: Bloquear início de procedimento sem orçamento aprovado
    Dado que o orçamento do plano de "Lucia Ramos" tem status "PENDENTE"
    Quando o dentista tenta marcar o procedimento "Extração" como "REALIZADO"
    Então o sistema deve bloquear a alteração
    E exibir a mensagem "O orçamento deve ser aprovado pelo paciente antes de iniciar procedimentos"

  Cenário: Aprovar orçamento e registrar data e forma de aprovação
    Dado que o orçamento de "Lucia Ramos" tem status "PENDENTE"
    Quando a recepcionista registra a aprovação do orçamento com a forma "Assinatura presencial" em "2026-05-10"
    Então o orçamento deve ter status "APROVADO"
    E a data "2026-05-10" e a forma "Assinatura presencial" devem ser registradas

  Cenário: Impedir alteração de orçamento aprovado
    Dado que o orçamento de "Lucia Ramos" tem status "APROVADO"
    Quando qualquer usuário tenta alterar o valor do procedimento "Extração"
    Então o sistema deve rejeitar a alteração
    E exibir a mensagem "Orçamentos aprovados não podem ser alterados retroativamente"

  Cenário: Gerar orçamento complementar ao revisar plano aprovado
    Dado que o orçamento de "Lucia Ramos" tem status "APROVADO"
    Quando o dentista adiciona o procedimento "Restauração" com valor R$ 300,00 ao plano em retorno
    Então o sistema deve gerar um novo orçamento complementar com valor R$ 300,00
    E o orçamento complementar deve ter o campo "complementar" marcado como verdadeiro
    E o orçamento original deve permanecer inalterado

  Cenário: Validar que valor total corresponde à soma dos procedimentos
    Dado que existe um plano de tratamento com procedimentos cujos valores somam R$ 400,00
    Quando o sistema gera o orçamento
    Então o valor total do orçamento deve ser exatamente R$ 400,00
