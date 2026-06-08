# language: pt

Funcionalidade: Cadastro de Instrumentos e Kits Odontológicos
  Como Auxiliar de Odontologia
  Eu quero cadastrar instrumentos individuais e kits odontológicos com suas configurações de validade
  Para que o controle de esterilização seja realizado com base em dados precisos e padronizados

  Cenário: Cadastro de instrumento individual com sucesso
    Quando o auxiliar cadastra o instrumento "Pinça Clínica Nº1" da categoria "Pinças" com código "PC-A01" e prazo de validade de 30 dias
    Então o instrumento deve ser salvo no sistema
    E o status do instrumento deve ser "ATIVO"
    E o tipo do instrumento deve ser "INDIVIDUAL"

  Cenário: Bloqueio de cadastro sem nome
    Quando o auxiliar tenta cadastrar um instrumento sem informar o nome
    Então o sistema deve rejeitar o cadastro do instrumento
    E a mensagem de erro do instrumento deve informar "Nome é obrigatório."

  Cenário: Bloqueio de cadastro sem categoria
    Quando o auxiliar tenta cadastrar um instrumento sem informar a categoria
    Então o sistema deve rejeitar o cadastro do instrumento
    E a mensagem de erro do instrumento deve informar "Categoria é obrigatória."

  Cenário: Bloqueio de cadastro sem código identificador
    Quando o auxiliar tenta cadastrar um instrumento sem informar o código
    Então o sistema deve rejeitar o cadastro do instrumento
    E a mensagem de erro do instrumento deve informar "Código é obrigatório."

  Cenário: Bloqueio de cadastro com código duplicado
    Dado que já existe um instrumento cadastrado com código "PC-A01"
    Quando o auxiliar tenta cadastrar outro instrumento com código "PC-A01"
    Então o sistema deve rejeitar o cadastro do instrumento
    E a mensagem de erro do instrumento deve informar "Código já cadastrado. Use um código único."

  Cenário: Desativação de instrumento impede aparição nos ciclos de esterilização
    Dado que existe um instrumento cadastrado com nome "Pinça Clínica Nº1" e status "ATIVO"
    Quando o auxiliar desativa o instrumento "Pinça Clínica Nº1"
    Então o status do instrumento deve ser "INATIVO"

  Cenário: Recálculo global de prazo de validade atualiza todos os instrumentos
    Dado que existe um instrumento cadastrado com nome "Pinça Clínica Nº1" e prazo de validade de 30 dias
    E que existe um instrumento cadastrado com nome "Espelho Bucal" e prazo de validade de 30 dias
    Quando o auxiliar altera o prazo global de validade para 15 dias
    Então o prazo de validade de "Pinça Clínica Nº1" deve ser 15 dias
    E o prazo de validade de "Espelho Bucal" deve ser 15 dias

  Cenário: Recálculo de prazo por categoria atualiza apenas instrumentos da categoria
    Dado que existe um instrumento cadastrado com nome "Pinça Clínica Nº1" da categoria "Pinças" e prazo de validade de 30 dias
    E que existe um instrumento cadastrado com nome "Espelho Bucal" da categoria "Espelhos e Sondas" e prazo de validade de 30 dias
    Quando o auxiliar altera o prazo da categoria "Pinças" para 45 dias
    Então o prazo de validade de "Pinça Clínica Nº1" deve ser 45 dias
    E o prazo de validade de "Espelho Bucal" deve ser 30 dias

  Cenário: Rejeição de prazo de validade inválido
    Quando o auxiliar tenta configurar o prazo global de validade para 0 dias
    Então o sistema deve rejeitar a configuração do prazo
    E a mensagem de erro do instrumento deve informar "Informe um número de dias válido (mínimo 1)."

  Cenário: Cadastro de kit vinculando instrumentos individuais
    Dado que existe um instrumento individual cadastrado com código "PC-A01"
    E que existe um instrumento individual cadastrado com código "ES-A01"
    Quando o auxiliar cadastra o kit "Kit Exame Padrão" da categoria "Kits de Exame" com código "KE-A01" composto pelos instrumentos "PC-A01" e "ES-A01"
    Então o instrumento deve ser salvo no sistema
    E o tipo do instrumento deve ser "KIT"
    E o kit deve conter os instrumentos "PC-A01" e "ES-A01"
