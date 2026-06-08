# language: pt

Funcionalidade: Status de Esterilização de Instrumentos
  Como Auxiliar de Odontologia
  Eu quero visualizar e atualizar o status de esterilização dos instrumentos
  Para garantir que apenas materiais dentro do prazo de validade sejam utilizados nos procedimentos

  Contexto:
    Dado que o instrumento "Fórceps 101" está cadastrado na F16 com prazo de validade de 7 dias e status ativo

  Cenário: Marcação de instrumento como Estéril registra data e responsável
    Dado que "Fórceps 101" está com status "CONTAMINADO"
    Quando o auxiliar "Lucas" marca "Fórceps 101" como Estéril na data de hoje
    Então o status do instrumento deve ser "ESTERIL"
    E a data da última esterilização deve ser registrada como hoje
    E o responsável deve ser registrado como "Lucas"
    E a data de vencimento deve ser calculada como hoje mais 7 dias

  Cenário: Instrumento é marcado automaticamente como Vencido após expirar o prazo
    Dado que "Fórceps 101" foi esterilizado há 8 dias com status "ESTERIL"
    Quando o sistema verifica a validade dos instrumentos
    Então o status de "Fórceps 101" deve ser atualizado para "VENCIDO"

  Cenário: Listagem de instrumentos prontos para uso filtra apenas Estéreis no prazo
    Dado que "Fórceps 101" está com status "ESTERIL" e dentro do prazo
    E que o instrumento "Espelho Bucal" está cadastrado na F16 com status ativo e status de esterilização "VENCIDO"
    E que o instrumento "Seringa Carpule" está cadastrado na F16 com status ativo e status de esterilização "CONTAMINADO"
    Quando o auxiliar solicita a lista de instrumentos prontos para uso
    Então a lista deve conter "Fórceps 101"
    E a lista não deve conter "Espelho Bucal"
    E a lista não deve conter "Seringa Carpule"

  Cenário: Instrumento inativo não aparece na lista de esterilização
    Dado que o instrumento "Bisturi Cabo 4" está cadastrado na F16 com status inativo
    Quando o auxiliar solicita a lista de instrumentos prontos para uso
    Então a lista não deve conter "Bisturi Cabo 4"

  Cenário: Marcação manual de instrumento como Contaminado após uso
    Dado que "Fórceps 101" está com status "ESTERIL"
    Quando o auxiliar marca "Fórceps 101" como Contaminado após uso no procedimento
    Então o status do instrumento deve ser "CONTAMINADO"
