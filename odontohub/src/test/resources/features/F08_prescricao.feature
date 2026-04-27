# language: pt

Funcionalidade: Registro de Medicamentos Prescritos
  Como Cirurgião-Dentista
  Eu quero registrar rapidamente os medicamentos que receitei para um paciente
  Para ter um histórico organizado e consultável de todas as prescrições

  Contexto:
    Dado que o paciente "Roberto Lima" está cadastrado no sistema
    E que o dentista "Dr. Carlos" está cadastrado no sistema

  Cenário: Registro de prescrição após atendimento com múltiplos medicamentos
    Quando o dentista "Dr. Carlos" registra a prescrição para "Roberto Lima" com os medicamentos:
      | Medicamento       | Dosagem | Período |
      | Amoxicilina 500mg | 1 comp  | 7 dias  |
      | Ibuprofeno 600mg  | 1 comp  | 3 dias  |
    E adiciona a observação "Tomar após as refeições"
    Então a prescrição deve ser salva com a data de hoje
    E deve estar vinculada ao perfil de "Roberto Lima"
    E deve conter a observação terapêutica informada

  Cenário: Consulta do histórico de prescrições em ordem cronológica
    Dado que "Roberto Lima" possui 3 prescrições registradas em datas diferentes
    Quando o dentista consulta o histórico de prescrições de "Roberto Lima"
    Então as prescrições devem ser listadas em ordem cronológica decrescente

  Cenário: Re-prescrição de medicamento anterior cria novo registro com data atual
    Dado que existe uma prescrição anterior de "Amoxicilina 500mg" registrada para "Roberto Lima"
    Quando o dentista repete a prescrição anterior para "Roberto Lima"
    Então uma nova prescrição deve ser criada com os mesmos medicamentos
    E a data da nova prescrição deve ser a data atual
    E a nova prescrição deve registrar a referência à prescrição de origem

  Cenário: Filtro de prescrições por período e dentista
    Dado que o dentista "Dr. Carlos" prescreveu "Amoxicilina" em janeiro de 2026
    E que o dentista "Dr. Carlos" prescreveu "Dipirona" em fevereiro de 2026
    Quando o dentista filtra suas prescrições do período de janeiro de 2026
    Então apenas a prescrição de "Amoxicilina" deve ser retornada
    E a prescrição de "Dipirona" não deve aparecer nos resultados
