# language: pt
Funcionalidade: F03 - Registro de Anamnese
  Como Cirurgião-Dentista
  Eu quero registrar a anamnese do paciente na primeira consulta
  Para que contraindicações e alergias sejam identificadas antes de qualquer procedimento

  Contexto:
    Dado que o paciente "Pedro Lima" está cadastrado no sistema

  Cenário: Registrar anamnese na primeira consulta
    Dado que o paciente "Pedro Lima" não possui anamnese registrada
    Quando o dentista registra a anamnese com alergia a "Penicilina" e contraindicação a "Anestésico com vasoconstritor"
    Então a anamnese deve ser salva com as informações fornecidas
    E a data e o responsável pelo registro devem ser gravados

  Cenário: Bloquear criação de plano de tratamento sem anamnese
    Dado que o paciente "Pedro Lima" não possui anamnese registrada
    Quando o dentista tenta criar um plano de tratamento para "Pedro Lima"
    Então o sistema deve bloquear a criação do plano
    E exibir a mensagem "É necessário registrar a anamnese antes de criar um plano de tratamento"

  Cenário: Exibir alerta de alergia em procedimentos subsequentes
    Dado que a anamnese de "Pedro Lima" registra alergia a "Penicilina"
    Quando o dentista tenta registrar um procedimento que utiliza "Amoxicilina" para "Pedro Lima"
    Então o sistema deve exibir um alerta de alergia
    E a mensagem deve informar "Paciente alérgico a substância relacionada: Penicilina"

  Cenário: Atualizar anamnese e manter histórico de versões
    Dado que o paciente "Pedro Lima" possui anamnese na versão 1
    Quando o dentista atualiza a anamnese adicionando alergia a "Dipirona"
    Então a anamnese deve ser atualizada para a versão 2
    E o histórico deve preservar a versão 1 com data e responsável pela alteração

  Cenário: Registrar data e responsável em cada atualização
    Dado que o paciente "Pedro Lima" possui anamnese registrada
    Quando a dentista "Dra. Ana" atualiza as condições sistêmicas da anamnese
    Então a atualização deve registrar a data atual e o nome "Dra. Ana" como responsável
