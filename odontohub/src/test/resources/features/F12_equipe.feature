# language: pt

Funcionalidade: Gestão de Equipe e Colaboradores
  Como Cirurgião-Dentista
  Eu quero cadastrar e gerenciar os dados de Auxiliares e Recepcionistas
  Para manter o controle sobre a equipe ativa e suas informações de contato

  Cenário: Cadastro de colaborador com função obrigatória e dados completos
    Quando o dentista cadastra o colaborador "Juliana Mendes" com CPF "123.456.789-00", telefone "81999998888" e função "Auxiliar"
    Então o colaborador deve ser salvo com status "Ativo"
    E a função "Auxiliar" deve estar registrada

  Cenário: Rejeição de cadastro sem função definida
    Quando o dentista tenta cadastrar um colaborador sem informar a função
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "A função do colaborador é obrigatória"

  Cenário: Rejeição de cadastro sem CPF informado
    Quando o dentista tenta cadastrar o colaborador "João" sem informar o CPF
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "CPF é obrigatório para o cadastro de colaboradores"

  Cenário: Desativação de colaborador preserva seus dados históricos
    Dado que o colaborador "Juliana Mendes" está com status "Ativo"
    Quando o dentista desativa o colaborador "Juliana Mendes"
    Então o status deve ser alterado para "Inativo"
    E os dados de "Juliana Mendes" devem permanecer no sistema

  Cenário: Colaborador inativo não aparece na lista de responsáveis para esterilização
    Dado que o colaborador "Juliana Mendes" com função "Auxiliar" está com status "Inativo"
    Quando o auxiliar abre a lista de responsáveis disponíveis para registro de esterilização
    Então "Juliana Mendes" não deve aparecer na lista

  Cenário: Apenas Auxiliares aparecem como responsáveis pela esterilização
    Dado que "Pedro Auxiliar" tem função "Auxiliar" e status "Ativo"
    E que "Maria Recepcionista" tem função "Recepcionista" e status "Ativo"
    Quando o sistema lista os responsáveis disponíveis para esterilização
    Então "Pedro Auxiliar" deve constar na lista
    E "Maria Recepcionista" não deve constar na lista

  Cenário: Reativação de colaborador previamente desativado
    Dado que o colaborador "Juliana Mendes" está com status "Inativo"
    Quando o dentista reativa o colaborador "Juliana Mendes"
    Então o status deve ser alterado para "Ativo"
    E "Juliana Mendes" deve voltar a aparecer nas listas de seleção

  Cenário: Permissões são atribuídas automaticamente conforme a função de Especialista
    Quando o dentista cadastra o colaborador "Dra. Helena" com CPF "111.222.333-44", telefone "81988887777" e função "Especialista"
    Então o colaborador "Dra. Helena" deve poder validar procedimentos
    E o colaborador "Dra. Helena" deve poder acessar dados financeiros

  Cenário: Auxiliar não acessa dados financeiros nem altera permissões
    Quando o dentista cadastra o colaborador "Carlos Auxiliar" com CPF "222.333.444-55", telefone "81977776666" e função "Auxiliar"
    Então o colaborador "Carlos Auxiliar" não deve poder acessar dados financeiros
    E o colaborador "Carlos Auxiliar" não deve poder alterar permissões

  Cenário: Apenas o Administrador pode alterar permissões
    Quando o dentista cadastra o colaborador "Ana Admin" com CPF "333.444.555-66", telefone "81966665555" e função "Administrador"
    Então o colaborador "Ana Admin" deve poder alterar permissões

  Cenário: Colaborador suspenso não pode realizar login
    Dado que existe o colaborador "Bruno Recepcao" com login "bruno" e senha "segredo123"
    E que o colaborador "Bruno Recepcao" está com status "Suspenso"
    Quando "Bruno Recepcao" tenta fazer login com a senha "segredo123"
    Então o login deve ser negado

  Cenário: Conta é bloqueada após múltiplas tentativas de login inválidas
    Dado que existe o colaborador "Diego Sistema" com login "diego" e senha "segredo123"
    Quando "Diego Sistema" erra a senha 3 vezes
    Então a conta de "Diego Sistema" deve estar bloqueada
    E "Diego Sistema" não deve conseguir login mesmo com a senha correta "segredo123"

  Cenário: Rejeição de cadastro com CPF duplicado
    Dado que existe o colaborador "Fernanda Lima" com CPF "999.888.777-66"
    Quando o dentista tenta cadastrar outro colaborador com o CPF "999.888.777-66"
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "Já existe um colaborador com este CPF"

  Cenário: Controle de disponibilidade por dia e horário de trabalho
    Dado que o colaborador "Sandra Recepcao" está cadastrado com função "Recepcionista"
    E que "Sandra Recepcao" tem disponibilidade de "SEGUNDA,TERCA,QUARTA,QUINTA,SEXTA" das 8h às 18h
    Então "Sandra Recepcao" deve estar disponível em "2026-06-15T10:00"
    E "Sandra Recepcao" não deve estar disponível em "2026-06-20T10:00"
    E "Sandra Recepcao" não deve estar disponível em "2026-06-15T20:00"

  Cenário: Período de férias torna o colaborador indisponível
    Dado que o colaborador "Sandra Recepcao" está cadastrado com função "Recepcionista"
    E que "Sandra Recepcao" tem disponibilidade de "SEGUNDA,TERCA,QUARTA,QUINTA,SEXTA" das 8h às 18h
    E que "Sandra Recepcao" registra ausência do tipo "FERIAS" de "2026-06-15" a "2026-06-19"
    Então "Sandra Recepcao" não deve estar disponível em "2026-06-15T10:00"

  Cenário: Auditoria registra ações e permite rastreabilidade por módulo
    Dado que o colaborador "Tiago Aux" está cadastrado com função "Auxiliar"
    Quando "Tiago Aux" registra a ação "Esterilizou kit cirúrgico" no módulo "ESTERILIZACAO"
    E "Tiago Aux" registra a ação "Criou agendamento" no módulo "AGENDAMENTO"
    Então a auditoria de "Tiago Aux" deve conter 2 registros
    E a rastreabilidade de "Tiago Aux" no módulo "ESTERILIZACAO" deve conter 1 registro

  Cenário: Alteração de telefone preserva o histórico cadastral
    Dado que o colaborador "Vera Lima" está cadastrado com função "Recepcionista"
    Quando o administrador altera o campo "telefone" de "Vera Lima" para "81911112222" como "Ana Admin"
    Então o histórico de alterações de "Vera Lima" deve conter 1 registro de alteração
    E o telefone de "Vera Lima" deve ser "81911112222"

  Cenário: Indicadores de desempenho do colaborador
    Dado que o colaborador "Will Dentista" está cadastrado com função "Especialista"
    Quando "Will Dentista" registra 4 atendimentos, 1 falta e 2 conversões
    Então a produtividade de "Will Dentista" deve ser 3
    E a taxa de conversão do colaborador "Will Dentista" deve ser 50%
