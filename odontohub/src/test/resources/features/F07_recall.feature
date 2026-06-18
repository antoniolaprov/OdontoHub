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

  Cenário: Priorização da fila de recall por risco clínico
    Dado que foi disparado um recall de "Cirurgia" para "Bruno Lima"
    E que foi disparado um recall de "Profilaxia" para "Carla Souza"
    E que foi disparado um recall de "Ortodontia" para "Diego Alves"
    Quando a recepcionista consulta a fila priorizada de recall
    Então "Bruno Lima" deve aparecer antes de "Diego Alves" na fila priorizada
    E "Diego Alves" deve aparecer antes de "Carla Souza" na fila priorizada
    E o nível de prioridade de "Bruno Lima" deve ser "ALTA"
    E o nível de prioridade de "Carla Souza" deve ser "BAIXA"

  Cenário: Escalonamento por SLA após tentativas de contato sem sucesso
    Dado que foi disparado um recall de "Implante" para "Bruno Lima"
    Quando a recepcionista registra 3 tentativas de contato sem sucesso para "Bruno Lima"
    Então o recall de "Bruno Lima" deve ser escalonado
    E o escalonamento deve registrar 3 tentativas

  Cenário: Métrica de taxa de conversão da fila de recall
    Dado que foi disparado um recall de "Profilaxia" para "Carla Souza"
    E que foi disparado um recall de "Profilaxia" para "Diego Alves"
    Quando "Carla Souza" é convertida em agendamento a partir do recall
    Então a taxa de conversão de recall deve ser 50%

  Cenário: Motor de priorização multifator ordena pelo maior risco acumulado
    Dado que foi disparado um recall de "Profilaxia" para "Elena Dias"
    E que foi disparado um recall de "Profilaxia" para "Fabio Rocha"
    E que o recall de "Fabio Rocha" possui fatores de risco com 12 meses sem retorno, inadimplente e risco clínico alto
    Quando a recepcionista consulta a fila priorizada de recall
    Então "Fabio Rocha" deve aparecer antes de "Elena Dias" na fila priorizada
    E a pontuação de prioridade de "Fabio Rocha" deve ser maior que a de "Elena Dias"

  Cenário: Segmentação automática de recall pós-operatório
    Dado que foi disparado um recall de "Cirurgia" para "Gustavo Neves"
    Então a categoria do recall de "Gustavo Neves" deve ser "POS_OPERATORIO"
    E o indicador visual de "Gustavo Neves" deve ser "AZUL"

  Cenário: Segmentação automática de recall financeiro por inadimplência
    Dado que foi disparado um recall de "Profilaxia" para "Helena Castro"
    E que o recall de "Helena Castro" possui fatores de inadimplência
    Então a categoria do recall de "Helena Castro" deve ser "FINANCEIRO"
    E o indicador visual de "Helena Castro" deve ser "ROXO"

  Cenário: Registro completo de tentativa de contato com retorno agendado
    Dado que foi disparado um recall de "Implante" para "Igor Mendes"
    Quando a recepcionista registra para "Igor Mendes" uma tentativa de contato por "WHATSAPP" com resultado "RETORNO_AGENDADO" pela responsável "Recepcao"
    Então o histórico de contato de "Igor Mendes" deve conter 1 tentativa de contato
    E o status do recall de "Igor Mendes" deve ser "Agendado"

  Cenário: SLA de atendimento da fila para casos críticos
    Dado que foi disparado um recall de "Cirurgia" para "Julia Pires"
    E que foi disparado um recall de "Profilaxia" para "Kevin Antunes"
    Quando a recepcionista consulta os recalls com SLA vencido daqui a 5 dias
    Então "Julia Pires" deve estar na lista de SLA vencido
    E "Kevin Antunes" não deve estar na lista de SLA vencido

  Cenário: Exclusão automática de paciente falecido da fila de recall
    Dado que foi disparado um recall de "Profilaxia" para "Lucas Brito"
    Quando o sistema exclui "Lucas Brito" do recall por motivo "FALECIDO"
    Então o status do recall de "Lucas Brito" deve ser "Excluido"
    E "Lucas Brito" não deve estar na fila priorizada

  Cenário: Métricas de pacientes recuperados e perdidos
    Dado que foi disparado um recall de "Profilaxia" para "Marina Lopes"
    E que foi disparado um recall de "Profilaxia" para "Nina Costa"
    Quando "Marina Lopes" é convertida em agendamento a partir do recall
    E o sistema exclui "Nina Costa" do recall por motivo "TRANSFERIDO"
    Então a métrica de pacientes recuperados deve ser 1
    E a métrica de pacientes perdidos deve ser 1
