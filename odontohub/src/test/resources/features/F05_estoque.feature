# language: pt

Funcionalidade: Controle de Estoque de Materiais Consumíveis
  Como Auxiliar
  Eu quero registrar a entrada de materiais e ter o consumo descontado automaticamente
  Para que o estoque seja mantido atualizado e o consultório não fique sem insumos essenciais

  Contexto:
    Dado que o material "Luva cirúrgica" está cadastrado com saldo de 100 unidades e ponto mínimo de 20

  Cenário: Registro de reposição atualiza saldo e gera saída no Fluxo de Caixa
    Quando o auxiliar registra a reposição de 50 unidades de "Luva cirúrgica" com custo unitário de R$1,50 do fornecedor "MedSupply"
    Então o saldo de "Luva cirúrgica" deve ser atualizado para 150 unidades
    E deve ser gerada uma saída automática de R$75,00 no Fluxo de Caixa na categoria "Insumos"

  Cenário: Rejeição de reposição com quantidade zero ou negativa
    Quando o auxiliar tenta registrar uma reposição de -10 unidades de "Luva cirúrgica"
    Então o sistema deve rejeitar a operação
    E a mensagem de erro deve informar "Quantidade e custo unitário devem ser valores positivos"

  Cenário: Rejeição de reposição com custo unitário zerado
    Quando o auxiliar tenta registrar uma reposição de 50 unidades de "Luva cirúrgica" com custo unitário de R$0,00
    Então o sistema deve rejeitar a operação
    E a mensagem de erro deve informar "Quantidade e custo unitário devem ser valores positivos"

  Cenário: Consumo automático de material ao realizar procedimento
    Dado que o procedimento "Extração" consome 2 unidades de "Luva cirúrgica"
    Quando o dentista realiza o procedimento "Extração"
    Então o saldo de "Luva cirúrgica" deve ser reduzido para 98 unidades

  Cenário: Alerta de estoque baixo ao atingir o ponto mínimo
    Dado que o saldo de "Luva cirúrgica" é de 22 unidades
    Quando 3 unidades de "Luva cirúrgica" são consumidas
    Então o saldo deve ser atualizado para 19 unidades
    E o sistema deve emitir um alerta de estoque baixo para "Luva cirúrgica"
    E o alerta deve informar o saldo atual de 19 e o ponto mínimo de 20
