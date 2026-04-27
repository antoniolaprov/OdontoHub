# language: pt
Funcionalidade: F14 - Registro de Manutenção de Equipamentos
  Como Cirurgião-Dentista
  Eu quero registrar as manutenções dos equipamentos e ser alertado quando uma manutenção preventiva estiver próxima do vencimento
  Para que os equipamentos estejam sempre disponíveis e em bom estado de funcionamento

  Contexto:
    Dado que o equipamento "Cadeira Odontológica" está cadastrado com status "DISPONIVEL"
    E com periodicidade de manutenção de 180 dias

  Cenário: Tornar equipamento indisponível ao iniciar manutenção
    Quando o dentista registra o início de uma manutenção corretiva na "Cadeira Odontológica"
    Então o status da "Cadeira Odontológica" deve ser alterado para "EM_MANUTENCAO"
    E o equipamento não deve poder ser associado a novos procedimentos

  Cenário: Bloquear uso de equipamento em manutenção em procedimento
    Dado que a "Cadeira Odontológica" tem status "EM_MANUTENCAO"
    Quando o dentista tenta vincular a "Cadeira Odontológica" a um procedimento
    Então o sistema deve bloquear a seleção do equipamento
    E exibir a mensagem "Equipamento indisponível: em manutenção"

  Cenário: Restaurar disponibilidade do equipamento ao concluir manutenção
    Dado que a "Cadeira Odontológica" tem status "EM_MANUTENCAO"
    Quando o dentista registra a conclusão da manutenção em "2026-05-10"
    Então o status da "Cadeira Odontológica" deve ser alterado para "DISPONIVEL"
    E a data da próxima manutenção deve ser recalculada para "2026-11-06"

  Cenário: Emitir alerta preventivo próximo ao vencimento da manutenção
    Dado que a próxima manutenção da "Cadeira Odontológica" está agendada para "2026-06-01"
    Quando o sistema verifica os equipamentos na data "2026-05-25"
    Então o sistema deve emitir um alerta ao dentista informando que a manutenção está próxima
    E o alerta deve indicar 7 dias restantes

  Cenário: Não emitir alerta quando manutenção está longe do vencimento
    Dado que a próxima manutenção da "Cadeira Odontológica" está agendada para "2026-12-01"
    Quando o sistema verifica os equipamentos na data "2026-05-25"
    Então nenhum alerta deve ser emitido para a "Cadeira Odontológica"

  Cenário: Registrar manutenção com todos os dados obrigatórios
    Quando o dentista registra uma manutenção preventiva na "Cadeira Odontológica" com:
      | Campo               | Valor                        |
      | Tipo                | PREVENTIVA                   |
      | Data de Início      | 2026-05-10                   |
      | Responsável Técnico | TechDental Serviços           |
      | Descrição           | Revisão geral e lubrificação |
      | Custo               | R$ 350,00                    |
    Então a manutenção deve ser salva e vinculada ao histórico patrimonial da "Cadeira Odontológica"

  Cenário: Bloquear registro de manutenção sem responsável técnico
    Quando o dentista tenta registrar uma manutenção sem informar o responsável técnico
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "Responsável técnico é obrigatório no registro de manutenção"

  Cenário: Dentista configura periodicidade de manutenção do equipamento
    Dado que o equipamento "Compressor" está cadastrado sem periodicidade definida
    Quando o dentista define a periodicidade de manutenção do "Compressor" como 90 dias
    Então a periodicidade do "Compressor" deve ser salva como 90 dias
    E a data da próxima manutenção deve ser calculada a partir da última manutenção registrada
