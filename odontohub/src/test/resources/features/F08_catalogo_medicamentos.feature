# language: pt

Funcionalidade: Cadastro e Gestão de Medicamentos
  Como Cirurgião-Dentista
  Eu quero cadastrar e manter um catálogo de medicamentos com seus dados clínicos e farmacológicos
  Para que as prescrições sejam emitidas com segurança, rastreabilidade e padronização

  Cenário: Cadastro de medicamento com dados obrigatórios
    Quando o dentista cadastra o medicamento "Amoxil" com princípio ativo "Amoxicilina", categoria "Antibiótico" e classe farmacológica "Beta-lactâmicos"
    Então o medicamento "Amoxil" deve ser salvo no catálogo
    E o status do medicamento deve ser "ATIVO"

  Cenário: Bloqueio de medicamento duplicado por nome comercial e princípio ativo
    Dado que existe o medicamento "Amoxil" com princípio ativo "Amoxicilina" e classe "Beta-lactâmicos"
    Quando o dentista tenta cadastrar o medicamento "Amoxil" com princípio ativo "Amoxicilina", categoria "Antibiótico" e classe farmacológica "Beta-lactâmicos"
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "Já existe um medicamento com o mesmo nome comercial e princípio ativo"

  Cenário: Bloqueio de cadastro sem princípio ativo
    Quando o dentista tenta cadastrar o medicamento "Tylenol" sem princípio ativo
    Então o sistema deve rejeitar o cadastro
    E a mensagem de erro deve informar "Princípio ativo é obrigatório"

  Cenário: Alerta de contraindicação cruzada por classe farmacológica
    Dado que existe o medicamento "Amoxil" com princípio ativo "Amoxicilina" e classe "Beta-lactâmicos"
    E que o paciente é alérgico a "Penicilina"
    Quando o dentista seleciona o medicamento "Amoxil" para prescrição
    Então o sistema deve emitir um alerta de contraindicação cruzada

  Cenário: Medicamento inativo não aparece para seleção mas permanece no catálogo
    Dado que existe o medicamento "Amoxil" com princípio ativo "Amoxicilina" e classe "Beta-lactâmicos"
    Quando o dentista inativa o medicamento "Amoxil" com a justificativa "Fora de linha"
    Então o medicamento "Amoxil" não deve aparecer na seleção de prescrição
    E o medicamento "Amoxil" deve permanecer no catálogo

  Cenário: Posologia padrão pode ser cadastrada e reutilizada
    Dado que existe o medicamento "Amoxil" com princípio ativo "Amoxicilina" e classe "Beta-lactâmicos"
    Quando o dentista cadastra a posologia padrão "1 cáps. a cada 8h por 7 dias" para "Amoxil"
    Então o medicamento "Amoxil" deve possuir a posologia padrão "1 cáps. a cada 8h por 7 dias"

  Cenário: Importação em lote rejeita linhas inválidas individualmente
    Quando o dentista importa o lote de medicamentos:
      | nomeComercial | principioAtivo | categoria   | classeFarmacologica |
      | Amoxil        | Amoxicilina    | Antibiótico | Beta-lactâmicos     |
      | Tylenol       |                | Analgésico  | Analgésicos         |
    Então o medicamento "Amoxil" deve ser importado com sucesso
    E a linha "Tylenol" deve ser rejeitada com o motivo "Princípio ativo é obrigatório"
