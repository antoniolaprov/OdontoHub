# language: pt

Funcionalidade: Cadastro de Pacientes
  Como Recepcionista
  Eu quero cadastrar e manter os dados dos pacientes no sistema
  Para que eles estejam disponíveis para agendamentos, prontuários e cobranças

  Cenário: Cadastro completo de paciente com todos os dados obrigatórios
    Quando a recepcionista cadastra o paciente "Ana Costa" com CPF "123.456.789-00" telefone "81 9 9999-1111" nascimento "12/03/1988" e email "ana.costa@email.com"
    Então o paciente deve ser salvo no sistema
    E o status do paciente deve ser "ATIVO"

  Cenário: Cadastro rápido sinaliza perfil como Incompleto
    Quando a recepcionista realiza um cadastro rápido com nome "Isabela Teixeira" e telefone "81 9 9111-9999"
    Então o paciente deve ser salvo no sistema
    E o status do paciente deve ser "INCOMPLETO"

  Cenário: Bloqueio de cadastro completo sem CPF
    Quando a recepcionista tenta cadastrar um paciente completo sem informar o CPF
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "CPF é obrigatório no cadastro completo."

  Cenário: Bloqueio de cadastro sem nome
    Quando a recepcionista tenta cadastrar um paciente sem informar o nome
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "Nome é obrigatório."

  Cenário: Bloqueio de cadastro sem telefone
    Quando a recepcionista tenta cadastrar um paciente sem informar o telefone
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "Telefone é obrigatório."

  Cenário: Bloqueio de cadastro com CPF duplicado
    Dado que já existe um paciente cadastrado com CPF "123.456.789-00"
    Quando a recepcionista tenta cadastrar outro paciente com CPF "123.456.789-00"
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "CPF já cadastrado."

  Cenário: Bloqueio de cadastro com e-mail duplicado
    Dado que já existe um paciente cadastrado com email "ana.costa@email.com"
    Quando a recepcionista tenta cadastrar outro paciente com email "ana.costa@email.com"
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "E-mail já cadastrado."

  Cenário: Atualização de cadastro registra histórico de alteração
    Dado que existe um paciente cadastrado com nome "Ana Costa" e telefone "81 9 9999-1111"
    Quando a recepcionista atualiza o telefone do paciente para "81 9 8888-0000" com responsável "Recepcionista"
    Então o cadastro do paciente deve refletir o novo telefone "81 9 8888-0000"
    E o histórico de alterações deve conter o campo "telefone" com valor anterior "81 9 9999-1111" e valor atualizado "81 9 8888-0000"
    E o responsável pela alteração deve ser registrado como "Recepcionista"
    E a data da alteração deve ser registrada

  Cenário: Restrição manual de paciente altera status para RESTRITO
    Dado que existe um paciente cadastrado com nome "Diego Alves" e status "ATIVO"
    Quando a recepcionista restringe o paciente "Diego Alves"
    Então o status do paciente deve ser "RESTRITO"
