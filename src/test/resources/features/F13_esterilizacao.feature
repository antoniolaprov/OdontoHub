# language: pt
Funcionalidade: F13 - Controle de Esterilização de Instrumentos
  Como Auxiliar
  Eu quero registrar os ciclos de esterilização dos instrumentos após cada uso
  Para que apenas instrumentos esterilizados sejam utilizados nos procedimentos

  Contexto:
    Dado que o instrumento "Sonda Exploradora" está cadastrado no sistema

  Cenário: Bloquear uso de instrumento pendente de esterilização
    Dado que o instrumento "Sonda Exploradora" tem status "PENDENTE_ESTERILIZACAO"
    Quando o dentista tenta vincular a "Sonda Exploradora" a um procedimento
    Então o sistema deve bloquear a seleção do instrumento
    E exibir a mensagem "Instrumento pendente de esterilização não pode ser utilizado"

  Cenário: Permitir uso de instrumento esterilizado
    Dado que o instrumento "Sonda Exploradora" tem status "ESTERILIZADO"
    Quando o dentista vincula a "Sonda Exploradora" ao procedimento "Extração"
    Então o instrumento deve ser vinculado ao procedimento com sucesso

  Cenário: Alterar status do instrumento para pendente ao confirmar procedimento
    Dado que o instrumento "Sonda Exploradora" tem status "ESTERILIZADO"
    E está vinculado ao procedimento "Extração"
    Quando o dentista registra o procedimento "Extração" como realizado
    Então o status da "Sonda Exploradora" deve ser alterado automaticamente para "PENDENTE_ESTERILIZACAO"

  Cenário: Registrar ciclo de esterilização com dados obrigatórios
    Dado que o instrumento "Sonda Exploradora" tem status "PENDENTE_ESTERILIZACAO"
    Quando a auxiliar registra a esterilização com método "AUTOCLAVE", data "2026-05-10" e responsável "Carla"
    Então o status da "Sonda Exploradora" deve ser alterado para "ESTERILIZADO"
    E o ciclo de esterilização deve ser salvo com método "AUTOCLAVE", data "2026-05-10" e responsável "Carla"

  Cenário: Bloquear esterilização sem método informado
    Quando a auxiliar tenta registrar esterilização sem informar o método
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "Método de esterilização é obrigatório"

  Cenário: Bloquear instrumento com esterilização vencida
    Dado que o instrumento "Sonda Exploradora" tem status "ESTERILIZADO" com validade em "2026-04-01"
    Quando o sistema verifica o instrumento na data "2026-04-02"
    Então o status da "Sonda Exploradora" deve ser alterado automaticamente para "VENCIDO"
    E o instrumento deve ser bloqueado para uso em procedimentos

  Esquema do Cenário: Validar prazo de validade por método de esterilização
    Dado que o instrumento "<instrumento>" foi esterilizado pelo método "<metodo>" em "<data_esterilizacao>"
    Então a validade da esterilização deve ser "<data_validade>"

    Exemplos:
      | instrumento      | metodo        | data_esterilizacao | data_validade |
      | Sonda            | AUTOCLAVE     | 2026-05-01         | 2026-11-01    |
      | Cureta           | ESTUFA        | 2026-05-01         | 2026-08-01    |
      | Alicate          | GLUTARALDEIDO | 2026-05-01         | 2026-05-15    |
