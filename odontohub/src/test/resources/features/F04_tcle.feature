# language: pt
Funcionalidade: F04 - Registro do Termo de Consentimento (TCLE)
  Como Cirurgião-Dentista
  Eu quero registrar o TCLE assinado pelo paciente antes de iniciar qualquer procedimento
  Para que o consentimento esteja formalizado e o consultório esteja juridicamente protegido

  Contexto:
    Dado que o paciente "Ana Ferreira" está cadastrado no sistema
    E que existe um plano de tratamento ativo para "Ana Ferreira"

  Cenário: Bloquear realização de procedimento sem TCLE assinado
    Dado que não existe TCLE assinado vinculado ao plano de tratamento de "Ana Ferreira"
    Quando o dentista tenta marcar um procedimento do plano como "REALIZADO"
    Então o sistema deve bloquear a alteração de status
    E exibir a mensagem "É necessário um TCLE assinado para registrar procedimentos realizados"

  Cenário: Registrar TCLE assinado vinculado ao plano de tratamento
    Dado que existe um plano de tratamento ativo para "Ana Ferreira"
    Quando o dentista registra o TCLE como assinado por "Ana Ferreira" em "2026-05-10"
    Então o TCLE deve ser salvo com status "ASSINADO"
    E deve estar vinculado ao plano de tratamento correspondente

  Cenário: Impedir registro de TCLE sem vínculo com plano de tratamento
    Quando o dentista tenta registrar um TCLE sem informar o plano de tratamento
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "O TCLE deve estar vinculado a um plano de tratamento"

  Cenário: Impedir exclusão de TCLE assinado
    Dado que existe um TCLE com status "ASSINADO" para "Ana Ferreira"
    Quando qualquer usuário tenta excluir o TCLE
    Então o sistema deve rejeitar a exclusão
    E exibir a mensagem "TCLEs assinados não podem ser excluídos"

  Cenário: Substituir TCLE com justificativa registrada
    Dado que existe um TCLE com status "ASSINADO" para "Ana Ferreira"
    Quando o dentista substitui o TCLE com a justificativa "Revisão do plano de tratamento"
    Então o TCLE antigo deve ter status alterado para "SUBSTITUIDO"
    E um novo TCLE deve ser criado com status "PENDENTE"
    E a justificativa de substituição deve ser registrada como "Revisão do plano de tratamento"

  Cenário: Permitir realização de procedimento com TCLE assinado
    Dado que existe um TCLE com status "ASSINADO" vinculado ao plano de "Ana Ferreira"
    Quando o dentista marca um procedimento do plano como "REALIZADO"
    Então o procedimento deve ter seu status alterado para "REALIZADO"
