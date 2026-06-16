# language: pt

Funcionalidade: Registro de Não Comparecimento
  Como Recepcionista
  Eu quero registrar quando um paciente falta a uma consulta
  Para acompanhar o absenteísmo e identificar pacientes reincidentes

  Cenário: Registro de falta injustificada em consulta passada
    Quando a recepcionista registra uma falta injustificada do paciente "Ana" no agendamento 201 ocorrida ontem
    Então o paciente "Ana" deve ter 1 falta registrada

  Cenário: Não é possível registrar falta de uma consulta futura
    Quando a recepcionista tenta registrar uma falta do paciente "Bruno" no agendamento 202 com data futura
    Então o sistema deve rejeitar a operação de falta
    E a mensagem de erro deve informar "Não é possível registrar falta de uma consulta futura"

  Cenário: Falta justificada não conta para reincidência
    Dado que o paciente "Carla" teve uma falta justificada no agendamento 203
    Então o paciente "Carla" não deve ser reincidente

  Cenário: Paciente torna-se reincidente após 3 faltas injustificadas
    Dado que o paciente "Diego" acumulou 3 faltas injustificadas
    Então o paciente "Diego" deve ser reincidente

  Cenário: Não é permitido registrar duas faltas para o mesmo agendamento
    Dado que o paciente "Elisa" teve uma falta injustificada no agendamento 205
    Quando a recepcionista tenta registrar outra falta no agendamento 205
    Então o sistema deve rejeitar a operação de falta
    E a mensagem de erro deve informar "Já existe um registro de não comparecimento para este agendamento"
