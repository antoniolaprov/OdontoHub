# language: pt

Funcionalidade: Status de Esterilização de Instrumentos
  Como Auxiliar de Odontologia
  Eu quero visualizar e atualizar o status de esterilização dos instrumentos
  Para garantir que apenas materiais dentro do prazo de validade sejam utilizados

  Contexto:
    Dado que o instrumento "Fórceps 101" está cadastrado com prazo de validade de 7 dias

  Cenário: Marcação de instrumento como Estéril registra data e responsável
    Quando o auxiliar "Lucas" marca "Fórceps 101" como Estéril na data de hoje
    Então o status do instrumento deve ser "Estéril"
    E a data da última esterilização deve ser registrada como hoje
    E o responsável deve ser registrado como "Lucas"
    E a data de vencimento deve ser calculada como hoje mais 7 dias

  Cenário: Instrumento é marcado automaticamente como Vencido após expirar o prazo
    Dado que "Fórceps 101" foi esterilizado há 8 dias com status "Estéril"
    Quando o sistema verifica a validade dos instrumentos
    Então o status de "Fórceps 101" deve ser atualizado para "Vencido"

  Cenário: Recalculo automático da validade ao alterar prazo global
    Dado que "Fórceps 101" foi esterilizado hoje com prazo global de 7 dias
    Quando o dentista altera o prazo global de esterilização para 15 dias
    Então a nova data de vencimento de "Fórceps 101" deve ser calculada como hoje mais 15 dias

  Cenário: Listagem de instrumentos prontos para uso filtra apenas Estéreis no prazo
    Dado que "Fórceps 101" está com status "Estéril" e dentro do prazo
    E que "Espelho bucal" está com status "Vencido"
    E que "Seringa carpule" está com status "Contaminado"
    Quando o auxiliar solicita a lista de instrumentos prontos para uso
    Então a lista deve conter apenas "Fórceps 101"
    E a lista não deve conter "Espelho bucal"
    E a lista não deve conter "Seringa carpule"

  Cenário: Marcação manual de instrumento como Contaminado após uso
    Dado que "Fórceps 101" está com status "Estéril"
    Quando o auxiliar marca "Fórceps 101" como Contaminado após uso no procedimento
    Então o status do instrumento deve ser "Contaminado"
