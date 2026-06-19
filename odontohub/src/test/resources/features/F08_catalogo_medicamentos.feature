# language: pt

Funcionalidade: F08 - Cadastro e Gestão de Medicamentos

  Como Cirurgião-Dentista
  Eu quero cadastrar e manter um catálogo de medicamentos com seus dados clínicos e farmacológicos
  Para que as prescrições sejam emitidas com segurança, rastreabilidade e padronização

  Contexto:
    Dado que existem as seguintes classes farmacológicas reconhecidas:
      | classe              |
      | Beta-lactâmicos     |
      | AINEs               |
      | Opioides            |
      | Corticosteroides    |
      | Analgésicos         |

  Cenário: Cadastrar medicamento com dados obrigatórios
    Quando eu cadastrar um medicamento com os dados:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao          | viaAdministracao | status | classeFarmacologica |
      | Amoxil        | Amoxicilina     | Antibiótico           | Cápsula 500mg         | Oral             | Ativo  | Beta-lactâmicos     |
    Então o medicamento "Amoxil" deve ser cadastrado com sucesso
    E o status do medicamento "Amoxil" deve ser "Ativo"

  Cenário: Impedir cadastro de medicamento com campos obrigatórios ausentes
    Quando eu tentar cadastrar um medicamento com os dados:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao | viaAdministracao | status | classeFarmacologica |
      |                | Amoxicilina     | Antibiótico           | Cápsula 500mg | Oral             | Ativo  | Beta-lactâmicos     |
    Então o sistema deve impedir o cadastro do medicamento
    E deve exibir a mensagem de erro "O nome comercial é obrigatório"

  Cenário: Impedir cadastro duplicado por nome comercial e princípio ativo
    Dado que já existe um medicamento cadastrado com nome comercial "Amoxil" e princípio ativo "Amoxicilina"
    Quando eu tentar cadastrar um medicamento com os dados:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao  | viaAdministracao | status | classeFarmacologica |
      | Amoxil        | Amoxicilina     | Antibiótico           | Cápsula 500mg | Oral             | Ativo  | Beta-lactâmicos     |
    Então o sistema deve impedir o cadastro do medicamento
    E deve exibir a mensagem de erro "Já existe medicamento cadastrado com este nome comercial e princípio ativo"

  Cenário: Vincular medicamento a uma classe farmacológica reconhecida
    Quando eu cadastrar um medicamento com os dados:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao      | viaAdministracao | status | classeFarmacologica |
      | Ibuprofeno    | Ibuprofeno      | Anti-inflamatório     | Comprimido 600mg  | Oral             | Ativo  | AINEs               |
    Então o medicamento "Ibuprofeno" deve estar vinculado à classe farmacológica "AINEs"

  Cenário: Impedir cadastro com classe farmacológica não reconhecida
    Quando eu tentar cadastrar um medicamento com os dados:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao     | viaAdministracao | status | classeFarmacologica |
      | TesteMed      | Substancia X    | Teste                 | Comprimido 10mg  | Oral             | Ativo  | Classe Inexistente  |
    Então o sistema deve impedir o cadastro do medicamento
    E deve exibir a mensagem de erro "Classe farmacológica não reconhecida"

  Cenário: Exibir contraindicações e interações ao selecionar medicamento na prescrição
    Dado que existe um medicamento "Amoxil" cadastrado com:
      | contraindicoes                  | interacoes                         |
      | Alergia a penicilina;Gestação   | Não combinar com anticoagulantes   |
    Quando o dentista selecionar o medicamento "Amoxil" durante a emissão da prescrição
    Então o sistema deve exibir as contraindicações do medicamento:
      | contraindicao         |
      | Alergia a penicilina  |
      | Gestação              |
    E deve exibir as interações medicamentosas:
      | interacao                           |
      | Não combinar com anticoagulantes    |

  Cenário: Alertar contraindicação cruzada com a anamnese do paciente
    Dado que existe um medicamento "Amoxil" da classe farmacológica "Beta-lactâmicos"
    E o paciente possui alergia registrada para "Penicilina"
    Quando o dentista selecionar o medicamento "Amoxil" durante a emissão da prescrição
    Então o sistema deve exibir o alerta "Paciente possui alergia relacionada à classe Beta-lactâmicos"

  Cenário: Medicamento inativo não deve aparecer para seleção na prescrição
    Dado que existe um medicamento "Codein" cadastrado com status "Inativo"
    Quando o dentista abrir a lista de medicamentos disponíveis para prescrição
    Então o medicamento "Codein" não deve aparecer na lista de seleção

  Cenário: Medicamento inativo permanece visível no histórico do prontuário
    Dado que existe uma prescrição histórica do medicamento "Codein" no prontuário do paciente
    E o medicamento "Codein" está com status "Inativo"
    Quando o dentista consultar o prontuário do paciente
    Então o medicamento "Codein" deve continuar visível no histórico de prescrições

  Cenário: Inativar medicamento sem prescrições recentes
    Dado que existe um medicamento "Ibuprofeno" cadastrado com status "Ativo"
    E o medicamento "Ibuprofeno" não possui prescrições ativas nos últimos 30 dias
    Quando eu inativar o medicamento "Ibuprofeno"
    Então o status do medicamento "Ibuprofeno" deve ser "Inativo"

  Cenário: Exigir justificativa ao inativar medicamento com prescrições recentes
    Dado que existe um medicamento "Amoxil" cadastrado com status "Ativo"
    E o medicamento "Amoxil" possui prescrições ativas nos últimos 30 dias
    Quando eu tentar inativar o medicamento "Amoxil" sem informar justificativa
    Então o sistema deve impedir a inativação do medicamento
    E deve exibir a mensagem de erro "Justificativa obrigatória para inativar medicamento com prescrições recentes"

  Cenário: Inativar medicamento com prescrições recentes e gerar alerta
    Dado que existe um medicamento "Amoxil" cadastrado com status "Ativo"
    E o medicamento "Amoxil" possui prescrições ativas nos últimos 30 dias
    Quando eu inativar o medicamento "Amoxil" com a justificativa "Medicamento substituído no protocolo clínico"
    Então o status do medicamento "Amoxil" deve ser "Inativo"
    E o sistema deve gerar um alerta para o dentista responsável pelas prescrições recentes

  Cenário: Cadastrar posologia padrão para medicamento
    Dado que existe um medicamento "Amoxil" cadastrado com status "Ativo"
    Quando eu cadastrar a posologia padrão "1 cápsula a cada 8h por 7 dias" para o medicamento "Amoxil"
    Então a posologia padrão deve ficar disponível para o medicamento "Amoxil"

  Cenário: Dentista pode ajustar a posologia padrão antes de salvar a prescrição
    Dado que existe um medicamento "Amoxil" com posologia padrão "1 cápsula a cada 8h por 7 dias"
    Quando o dentista selecionar a posologia padrão do medicamento "Amoxil"
    E ajustar a posologia para "1 cápsula a cada 12h por 5 dias"
    Então a prescrição deve ser salva com a posologia "1 cápsula a cada 12h por 5 dias"

  Cenário: Exibir painel consolidado de uso do medicamento
    Dado que existe um medicamento "Amoxil" cadastrado
    E existem prescrições registradas para o medicamento "Amoxil"
    Quando eu consultar a ficha do medicamento "Amoxil"
    Então o sistema deve exibir o painel consolidado de uso contendo:
      | campo                    |
      | total de vezes prescrito |
      | período de maior uso     |
      | dentistas que mais prescreveram |
      | quantidade de pacientes que receberam o medicamento |

  Cenário: Gerar auditoria ao editar medicamento
    Dado que existe um medicamento "Amoxil" cadastrado com categoria terapêutica "Antibiótico"
    Quando o usuário "Dr. João" alterar a categoria terapêutica do medicamento "Amoxil" para "Antimicrobiano"
    Então o sistema deve gerar log de auditoria contendo:
      | usuario | campoAlterado          | valorAnterior | valorAtualizado | medicamento |
      | Dr. João | categoriaTerapeutica   | Antibiótico   | Antimicrobiano  | Amoxil      |

  Cenário: Impedir alteração de classe farmacológica com prescrições nos últimos 90 dias
    Dado que existe um medicamento "Amoxil" da classe farmacológica "Beta-lactâmicos"
    E o medicamento "Amoxil" possui prescrições emitidas nos últimos 90 dias
    Quando eu tentar alterar a classe farmacológica do medicamento "Amoxil" para "AINEs"
    Então o sistema deve impedir a alteração da classe farmacológica
    E deve exibir a mensagem de erro "Classe farmacológica não pode ser alterada pois há prescrições emitidas nos últimos 90 dias"

  Cenário: Importar medicamentos válidos via CSV
    Quando eu importar uma planilha CSV com as linhas:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao     | viaAdministracao | status | classeFarmacologica |
      | Amoxil        | Amoxicilina     | Antibiótico           | Cápsula 500mg    | Oral             | Ativo  | Beta-lactâmicos     |
      | Ibuprofeno    | Ibuprofeno      | Anti-inflamatório     | Comprimido 600mg | Oral             | Ativo  | AINEs               |
    Então o sistema deve importar 2 medicamento(s) com sucesso
    E não deve registrar erros de importação

  Cenário: Rejeitar individualmente linhas inválidas na importação CSV
    Quando eu importar uma planilha CSV com as linhas:
      | nomeComercial | principioAtivo | categoriaTerapeutica | apresentacao     | viaAdministracao | status | classeFarmacologica |
      | Amoxil        | Amoxicilina     | Antibiótico           | Cápsula 500mg    | Oral             | Ativo  | Beta-lactâmicos     |
      |               | Ibuprofeno      | Anti-inflamatório     | Comprimido 600mg | Oral             | Ativo  | AINEs               |
      | TesteMed      | Substancia X    | Teste                 | Comprimido 10mg  | Oral             | Ativo  | Classe Inexistente  |
    Então o sistema deve importar 1 medicamento(s) com sucesso
    E deve rejeitar 2 linha(s) com erro
    E deve registrar os erros de importação:
      | erro                                  |
      | O nome comercial é obrigatório        |
      | Classe farmacológica não reconhecida  |