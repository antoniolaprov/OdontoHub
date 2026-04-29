# language: pt

Funcionalidade: Gestão e Execução do Plano de Tratamento
  Como Cirurgião-Dentista
  Eu quero elaborar um plano de tratamento e registrar o histórico de procedimentos realizados
  Para coordenar as etapas do tratamento e garantir a validade jurídica do prontuário

  Contexto:
    Dado que o paciente "Pedro Santos" possui anamnese registrada no sistema
    E que existe um agendamento confirmado para "Pedro Santos"

Cenário: Criação de Plano de Tratamento quando paciente possui anamnese

    Quando o dentista elabora um Plano de Tratamento para "Pedro Santos"
    Então o plano deve ser criado com status "Em Andamento"
    E o plano deve ser criado na versão 1

  Cenário: Bloqueio de criação de Plano para paciente sem anamnese
    Dado que o paciente "Carlos Sem Anamnese" não possui anamnese registrada no sistema

    Quando o dentista tenta elaborar um Plano de Tratamento para "Carlos Sem Anamnese"
    Então o sistema do plano deve bloquear a operação
    E a mensagem de erro do plano deve informar "Paciente não possui anamnese registrada"
  Cenário: Realização de procedimento exige evolução clínica e agendamento vinculado
    Dado que o Plano de Tratamento de "Pedro Santos" possui o procedimento "Profilaxia" com status "Pendente"
    Quando o dentista realiza o procedimento "Profilaxia" com a evolução clínica "Limpeza realizada sem intercorrências"
    Então o status do procedimento deve ser "Realizado"
    E a evolução clínica deve ser registrada com data e executor
    E o agendamento confirmado deve estar vinculado ao procedimento

  Cenário: Cancelamento de procedimento exige justificativa e não gera histórico
    Dado que o Plano de Tratamento de "Pedro Santos" possui o procedimento "Restauração" com status "Pendente"
    Quando o dentista cancela o procedimento "Restauração" com justificativa "Paciente recusou o procedimento"
    Então o status do procedimento deve ser "Cancelado"
    E a justificativa de cancelamento deve ser armazenada
    E nenhum registro deve ser gerado na Ficha Clínica para este procedimento

  Cenário: Adição de procedimento ao plano incrementa a versão
    Dado que o Plano de Tratamento de "Pedro Santos" está na versão 1
    Quando o dentista adiciona o procedimento "Restauração" ao plano
    Então o plano deve ser atualizado para a versão 2

  Cenário: Exclusão de procedimento realizado dentro da janela de 24 horas
    Dado que o procedimento "Extração" foi registrado como "Realizado" há 2 horas no plano de "Pedro Santos"
    Quando o dentista exclui o procedimento "Extração" com a justificativa "Registro incorreto"
    Então o procedimento deve ser removido do histórico
    E a justificativa deve ser armazenada no log de auditoria

  Cenário: Bloqueio definitivo de exclusão após 24 horas
    Dado que o procedimento "Extração" foi registrado como "Realizado" há 25 horas no plano de "Pedro Santos"
    Quando o dentista tenta excluir o procedimento "Extração"
    Então o sistema do plano deve bloquear a exclusão
    E a mensagem de erro do plano deve informar "Prazo de 24 horas para correção expirado. O registro é imutável"

  Cenário: Encerramento prematuro do plano exige justificativa vinculada ao prontuário
    Dado que o Plano de Tratamento de "Pedro Santos" possui procedimentos com status "Pendente"
    Quando o dentista encerra o plano com a justificativa "Paciente interrompeu o tratamento"
    Então o status do plano deve ser "Encerrado"
    E a justificativa de encerramento deve estar vinculada ao prontuário vitalício do paciente

  Cenário: Bloqueio de exclusão do plano com procedimentos no histórico fora da janela
    Dado que o Plano de Tratamento de "Pedro Santos" possui um procedimento realizado há 48 horas
    Quando o dentista tenta excluir o Plano de Tratamento
    Então o sistema do plano deve bloquear a exclusão
    E a mensagem de erro do plano deve informar "Plano possui procedimentos no histórico que não podem ser removidos"