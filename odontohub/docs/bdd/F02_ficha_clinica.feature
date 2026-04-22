# language: pt
Funcionalidade: F02 - Gestão da Ficha Clínica
  Como Cirurgião-Dentista
  Eu quero registrar as informações clínicas de cada atendimento no prontuário do paciente
  Para que o histórico clínico esteja sempre disponível e seja juridicamente válido

  Contexto:
    Dado que o paciente "Maria Souza" está cadastrado no sistema
    E que existe um agendamento confirmado para "Maria Souza"

  Cenário: Criar prontuário automaticamente no primeiro atendimento
    Dado que o paciente "Maria Souza" não possui prontuário
    Quando o dentista registra a primeira ficha clínica para "Maria Souza"
    Então o sistema deve criar automaticamente um prontuário para "Maria Souza"
    E a ficha clínica deve ser vinculada ao prontuário criado
    E o prontuário deve ter status "ATIVO"

  Cenário: Criar nova ficha clínica em prontuário existente
    Dado que o paciente "Maria Souza" já possui um prontuário ativo
    Quando o dentista registra um novo atendimento para "Maria Souza"
    Então o sistema deve criar uma nova ficha clínica dentro do prontuário existente
    E não deve criar um novo prontuário

  Cenário: Impedir edição de evolução clínica após confirmação
    Dado que existe uma ficha clínica confirmada para "Maria Souza" com a evolução "Extração realizada sem intercorrências"
    Quando o dentista tenta editar a evolução "Extração realizada sem intercorrências"
    Então o sistema deve rejeitar a edição
    E exibir a mensagem "Evoluções confirmadas não podem ser editadas"

  Cenário: Exigir vínculo com agendamento confirmado
    Dado que não existe agendamento confirmado para "Maria Souza" na data atual
    Quando o dentista tenta registrar uma ficha clínica para "Maria Souza"
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "É necessário um agendamento confirmado para registrar atendimento"

  Cenário: Encerrar prontuário com justificativa
    Dado que o prontuário de "Maria Souza" tem status "ATIVO"
    Quando o dentista encerra o prontuário com a justificativa "Paciente transferido para outra clínica"
    Então o status do prontuário deve ser alterado para "ENCERRADO"
    E a justificativa deve ser registrada

  Cenário: Impedir exclusão de prontuário
    Dado que existe um prontuário para "Maria Souza"
    Quando qualquer usuário tenta excluir o prontuário de "Maria Souza"
    Então o sistema deve rejeitar a exclusão
    E exibir a mensagem "Prontuários não podem ser excluídos, apenas encerrados"
