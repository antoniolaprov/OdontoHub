# language: pt

Funcionalidade: Cadastro e Login de Clínica
  Como administrador de uma clínica odontológica
  Eu quero cadastrar minha clínica e autenticar com e-mail e senha
  Para que apenas usuários autorizados acessem o OdontoHub

  Cenário: Cadastro de clínica com todos os dados obrigatórios
    Quando o administrador cadastra a clínica "Sorriso Feliz" com CNPJ "11.111.111/0001-11" email "contato@sorrisofeliz.com" e senha "segredo123"
    Então a clínica deve ser salva no sistema
    E o status da clínica deve ser "ATIVA"
    E a senha da clínica não deve ser armazenada em texto puro

  Cenário: Bloqueio de cadastro de clínica sem nome
    Quando o administrador tenta cadastrar uma clínica sem informar o nome
    Então o sistema deve rejeitar o cadastro da clínica
    E a mensagem de erro da clínica deve informar "Nome da clínica é obrigatório."

  Cenário: Bloqueio de cadastro de clínica com e-mail inválido
    Quando o administrador tenta cadastrar uma clínica com o email "email-invalido"
    Então o sistema deve rejeitar o cadastro da clínica
    E a mensagem de erro da clínica deve informar "E-mail inválido."

  Cenário: Bloqueio de cadastro de clínica com senha curta
    Quando o administrador tenta cadastrar uma clínica com a senha "123"
    Então o sistema deve rejeitar o cadastro da clínica
    E a mensagem de erro da clínica deve informar "Senha deve ter no mínimo 6 caracteres."

  Cenário: Bloqueio de cadastro de clínica com e-mail duplicado
    Dado que já existe uma clínica cadastrada com email "contato@sorrisofeliz.com"
    Quando o administrador tenta cadastrar outra clínica com email "contato@sorrisofeliz.com"
    Então o sistema deve rejeitar o cadastro da clínica
    E a mensagem de erro da clínica deve informar "E-mail já cadastrado."

  Cenário: Bloqueio de cadastro de clínica com CNPJ duplicado
    Dado que já existe uma clínica cadastrada com CNPJ "11.111.111/0001-11"
    Quando o administrador tenta cadastrar outra clínica com CNPJ "11.111.111/0001-11"
    Então o sistema deve rejeitar o cadastro da clínica
    E a mensagem de erro da clínica deve informar "CNPJ já cadastrado."

  Cenário: Login com credenciais corretas
    Dado que existe uma clínica cadastrada com email "contato@sorrisofeliz.com" e senha "segredo123"
    Quando a clínica faz login com email "contato@sorrisofeliz.com" e senha "segredo123"
    Então o login da clínica deve ser bem-sucedido
    E a clínica autenticada deve ser "Sorriso Feliz"

  Cenário: Login com senha incorreta é rejeitado
    Dado que existe uma clínica cadastrada com email "contato@sorrisofeliz.com" e senha "segredo123"
    Quando a clínica tenta fazer login com email "contato@sorrisofeliz.com" e senha "senhaErrada"
    Então o login da clínica deve ser rejeitado
    E a mensagem de erro da clínica deve informar "E-mail ou senha inválidos."

  Cenário: Login com e-mail inexistente é rejeitado
    Quando a clínica tenta fazer login com email "naoexiste@clinica.com" e senha "qualquer123"
    Então o login da clínica deve ser rejeitado
    E a mensagem de erro da clínica deve informar "E-mail ou senha inválidos."

  Cenário: Login é insensível a maiúsculas no e-mail
    Dado que existe uma clínica cadastrada com email "contato@sorrisofeliz.com" e senha "segredo123"
    Quando a clínica faz login com email "CONTATO@SORRISOFELIZ.COM" e senha "segredo123"
    Então o login da clínica deve ser bem-sucedido
