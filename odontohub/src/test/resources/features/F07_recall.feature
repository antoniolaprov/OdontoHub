# language: pt

Funcionalidade: Automação e Fila de Recall
  Como Recepcionista
  Eu quero visualizar uma lista gerada automaticamente de pacientes para contato de retorno
  Para realizar agendamentos preventivos com base no histórico clínico

  Contexto:
    Dado que o paciente "Ana Ferreira" está cadastrado no sistema

  Cenário: Disparo automático de recall após procedimento de Profilaxia
    Dado que o dentista realizou o procedimento "Profilaxia" para "Ana Ferreira"
    Quando o sistema processa os gatilhos de recall
    Então "Ana Ferreira" deve ser inserida na fila de recall com prazo de 180 dias
    E o status do recall deve ser "Na Fila"

  Cenário: Disparo de recall com prazo diferente para Implante
    Dado que o dentista realizou o procedimento "Implante" para "Ana Ferreira"
    Quando o sistema processa os gatilhos de recall
    Então "Ana Ferreira" deve ser inserida na fila de recall com prazo de 45 dias

  Cenário: Recall cancelado quando paciente já possui agendamento futuro
    Dado que "Ana Ferreira" está na fila de recall com status "Na Fila"
    E que "Ana Ferreira" possui um agendamento futuro confirmado
    Quando o sistema verifica sobreposição de agendamentos para recall
    Então "Ana Ferreira" deve ser removida da fila de recall
    E o evento de cancelamento deve registrar o ID do agendamento existente

  Cenário: Agendamento gerado via recall recebe flag de conversão
    Dado que "Ana Ferreira" está na fila de recall com status "Na Fila"
    Quando a recepcionista agenda "Ana Ferreira" diretamente da tela de Recall
    Então o novo agendamento deve ser marcado com a flag de conversão de recall
    E o status do recall deve ser atualizado para "Convertido"
