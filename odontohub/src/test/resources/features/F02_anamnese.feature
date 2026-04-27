# language: pt

Funcionalidade: Registro de Anamnese
  Como Cirurgião-Dentista
  Eu quero registrar a anamnese do paciente na primeira consulta
  Para que contraindicações e alergias sejam identificadas antes de qualquer procedimento

  Contexto:
    Dado que o paciente "Maria Oliveira" está cadastrado no sistema

  Cenário: Registro de anamnese na primeira consulta salva versão inicial
    Quando o dentista registra a anamnese de "Maria Oliveira" com alergia a "Amoxicilina" e condição sistêmica "Hipertensão"
    Então a anamnese deve ser salva com versão 1
    E a data de registro deve ser armazenada
    E o responsável pelo cadastro deve ser registrado

  Cenário: Bloqueio de criação de Plano de Tratamento sem anamnese registrada
    Dado que o paciente "Maria Oliveira" não possui anamnese registrada
    Quando o dentista tenta criar um Plano de Tratamento para "Maria Oliveira"
    Então o sistema deve bloquear a criação do plano
    E a mensagem de erro deve informar "Paciente não possui anamnese registrada"

  Cenário: Alerta automático de alergia por família farmacológica
    Dado que o paciente "Maria Oliveira" possui anamnese com alergia a "Penicilina"
    Quando o dentista verifica a substância "Amoxicilina" para uso em procedimento de "Maria Oliveira"
    Então o sistema deve emitir um alerta de alergia
    E o alerta deve informar que "Amoxicilina" pertence à família farmacológica "Penicilina"

  Cenário: Atualização de anamnese preserva histórico de versões anteriores
    Dado que o paciente "Maria Oliveira" possui anamnese registrada na versão 1 com alergia a "Penicilina"
    Quando o dentista adiciona a alergia "Dipirona" à anamnese de "Maria Oliveira"
    Então a anamnese deve ser atualizada para a versão 2
    E o histórico deve conter a versão 1 com os dados anteriores
    E a data e o responsável pela atualização devem ser registrados

  Cenário: Adição de nova alergia não remove as alergias existentes
    Dado que o paciente "Maria Oliveira" possui anamnese com alergia a "Penicilina"
    Quando o dentista adiciona a alergia "Látex" à anamnese de "Maria Oliveira"
    Então a anamnese deve conter as alergias "Penicilina" e "Látex"
