# language: pt

Funcionalidade: Gestão de Equipe e Colaboradores
Como Cirurgião-Dentista
Eu quero cadastrar e gerenciar os dados de Auxiliares e Recepcionistas
Para manter o controle sobre a equipe ativa e suas informações de contato

Cenário: Cadastro de colaborador com função obrigatória e dados completos
Quando o dentista cadastra o colaborador "Juliana Mendes" com CPF "123.456.789-00", telefone "81999998888" e função "Auxiliar"
Então o colaborador deve ser salvo com status "Ativo"
E a função "Auxiliar" deve estar registrada

Cenário: Rejeição de cadastro sem função definida
Quando o dentista tenta cadastrar um colaborador sem informar a função
Então o sistema deve rejeitar o cadastro
E a mensagem de erro de colaborador deve informar "A função do colaborador é obrigatória"

Cenário: Rejeição de cadastro sem CPF informado
Quando o dentista tenta cadastrar o colaborador "João" sem informar o CPF
Então o sistema deve rejeitar o cadastro
E a mensagem de erro de colaborador deve informar "CPF é obrigatório para o cadastro de colaboradores"

Cenário: Desativação de colaborador preserva seus dados históricos
Dado que o colaborador "Juliana Mendes" está com status "Ativo"
Quando o dentista desativa o colaborador "Juliana Mendes"
Então o status deve ser alterado para "Inativo"
E os dados de "Juliana Mendes" devem permanecer no sistema

Cenário: Colaborador inativo não aparece na lista de responsáveis para esterilização
Dado que o colaborador "Juliana Mendes" com função "Auxiliar" está com status "Inativo"
Quando o auxiliar abre a lista de responsáveis disponíveis para registro de esterilização
Então "Juliana Mendes" não deve aparecer na lista

Cenário: Apenas Auxiliares aparecem como responsáveis pela esterilização
Dado que "Pedro Auxiliar" tem função "Auxiliar" e status "Ativo"
E que "Maria Recepcionista" tem função "Recepcionista" e status "Ativo"
Quando o sistema lista os responsáveis disponíveis para esterilização
Então "Pedro Auxiliar" deve constar na lista
E "Maria Recepcionista" não deve constar na lista

Cenário: Reativação de colaborador previamente desativado
Dado que o colaborador "Juliana Mendes" está com status "Inativo"
Quando o dentista reativa o colaborador "Juliana Mendes"
Então o status deve ser alterado para "Ativo"
E "Juliana Mendes" deve voltar a aparecer nas listas de seleção