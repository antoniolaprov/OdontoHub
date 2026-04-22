# language: pt
Funcionalidade: F05 - Elaboração do Plano de Tratamento
  Como Cirurgião-Dentista
  Eu quero elaborar um plano de tratamento com os procedimentos necessários para o paciente
  Para que o tratamento seja organizado, sequenciado e acompanhado de forma estruturada

  Contexto:
    Dado que o paciente "Carlos Mendes" está cadastrado no sistema
    E que o paciente "Carlos Mendes" possui anamnese registrada

  Cenário: Criar plano de tratamento com anamnese registrada
    Dado que o paciente "Carlos Mendes" possui anamnese registrada
    Quando o dentista cria um plano de tratamento com os procedimentos "Extração" e "Limpeza"
    Então o plano de tratamento deve ser criado com status "ATIVO"
    E os procedimentos devem ter status inicial "PENDENTE"
    E o plano deve ser registrado na versão 1

  Cenário: Bloquear criação de plano sem anamnese
    Dado que o paciente "Carlos Mendes" não possui anamnese registrada
    Quando o dentista tenta criar um plano de tratamento para "Carlos Mendes"
    Então o sistema deve bloquear a criação
    E exibir a mensagem "É necessário registrar a anamnese antes de criar um plano de tratamento"

  Cenário: Cancelar procedimento com justificativa obrigatória
    Dado que existe um plano de tratamento ativo com o procedimento "Restauração" com status "PENDENTE"
    Quando o dentista cancela o procedimento "Restauração" sem informar justificativa
    Então o sistema deve rejeitar o cancelamento
    E exibir a mensagem "Justificativa é obrigatória para cancelar um procedimento"

  Cenário: Cancelar procedimento com justificativa válida
    Dado que existe um plano de tratamento ativo com o procedimento "Restauração" com status "PENDENTE"
    Quando o dentista cancela o procedimento "Restauração" com a justificativa "Dente extraído"
    Então o status do procedimento deve ser alterado para "CANCELADO"
    E a justificativa "Dente extraído" deve ser registrada

  Cenário: Concluir plano de tratamento com todos procedimentos finalizados
    Dado que existe um plano de tratamento com os procedimentos:
      | Procedimento | Status    |
      | Extração     | REALIZADO |
      | Limpeza      | REALIZADO |
    Quando o dentista marca o plano como concluído
    Então o status do plano deve ser alterado para "CONCLUIDO"

  Cenário: Bloquear conclusão de plano com procedimento pendente
    Dado que existe um plano de tratamento com os procedimentos:
      | Procedimento | Status    |
      | Extração     | REALIZADO |
      | Limpeza      | PENDENTE  |
    Quando o dentista tenta marcar o plano como concluído
    Então o sistema deve rejeitar a conclusão
    E exibir a mensagem "Existem procedimentos pendentes no plano de tratamento"

  Cenário: Gerar nova versão do plano ao revisar em retorno
    Dado que existe um plano de tratamento na versão 1 para "Carlos Mendes"
    Quando o dentista adiciona o procedimento "Clareamento" ao plano em um retorno
    Então o plano deve ser atualizado para a versão 2
    E o histórico deve preservar os dados da versão 1

  Cenário: Impedir exclusão de plano com procedimentos realizados
    Dado que existe um plano de tratamento com o procedimento "Extração" com status "REALIZADO"
    Quando qualquer usuário tenta excluir o plano de tratamento
    Então o sistema deve rejeitar a exclusão
    E exibir a mensagem "Planos com procedimentos realizados não podem ser excluídos"
