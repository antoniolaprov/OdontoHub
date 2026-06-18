# language: pt

Funcionalidade: Confirmação e Lembretes de Consulta
  Como Recepcionista
  Eu quero enviar lembretes de consulta e registrar a confirmação do paciente
  Para reduzir faltas e manter a agenda organizada

  Cenário: Geração de lembrete para uma consulta agendada
    Quando a recepcionista gera um lembrete para o agendamento 101 do paciente "Ana" pelo canal "WhatsApp"
    Então o lembrete do agendamento 101 deve estar com status "Pendente"

  Cenário: Não é permitido gerar lembrete duplicado para o mesmo agendamento
    Dado que existe um lembrete para o agendamento 102 do paciente "Bruno" pelo canal "SMS"
    Quando a recepcionista tenta gerar outro lembrete para o agendamento 102
    Então o sistema deve rejeitar a operação de lembrete
    E a mensagem de erro deve informar "Já existe um lembrete para este agendamento"

  Cenário: Envio do lembrete ao paciente
    Dado que existe um lembrete para o agendamento 103 do paciente "Carla" pelo canal "Email"
    Quando o sistema envia o lembrete do agendamento 103
    Então o lembrete do agendamento 103 deve estar com status "Enviado"

  Cenário: Paciente confirma a consulta pelo lembrete
    Dado que existe um lembrete enviado para o agendamento 104 do paciente "Diego"
    Quando o paciente confirma a consulta do agendamento 104
    Então o lembrete do agendamento 104 deve estar com status "Confirmado"

  Cenário: Não é possível confirmar um lembrete que não foi enviado
    Dado que existe um lembrete para o agendamento 105 do paciente "Elisa" pelo canal "SMS"
    Quando o paciente tenta confirmar a consulta do agendamento 105
    Então o sistema deve rejeitar a operação de lembrete
    E a mensagem de erro deve informar "Só é possível confirmar um lembrete que já foi enviado"

  Cenário: Paciente recusa o lembrete
    Dado que existe um lembrete enviado para o agendamento 106 do paciente "Fábio"
    Quando o paciente recusa o lembrete do agendamento 106 com o motivo "Vou viajar"
    Então o lembrete do agendamento 106 deve estar com status "Recusado"
