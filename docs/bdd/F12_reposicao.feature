# language: pt
Funcionalidade: F12 - Registro de Reposição de Materiais
  Como Auxiliar
  Eu quero registrar a reposição de materiais consumíveis após uma compra
  Para que o saldo do estoque seja atualizado e o custo da compra seja registrado no fluxo de caixa

  Contexto:
    Dado que o material "Resina Composta" está cadastrado com saldo de 5 unidades

  Cenário: Registrar reposição e atualizar saldo imediatamente
    Quando a auxiliar registra a reposição de 20 unidades de "Resina Composta" do fornecedor "DentalPro" com custo unitário R$ 15,00
    Então o saldo de "Resina Composta" deve ser atualizado imediatamente para 25 unidades
    E a reposição deve ser salva com fornecedor "DentalPro", quantidade 20 e custo total R$ 300,00

  Cenário: Lançar custo da reposição como saída no fluxo de caixa
    Quando a auxiliar registra a reposição de 20 unidades de "Resina Composta" com custo total de R$ 300,00
    Então um lançamento de saída de R$ 300,00 deve ser criado automaticamente no fluxo de caixa
    E o lançamento deve ter categoria "Insumos"

  Cenário: Bloquear reposição sem fornecedor informado
    Quando a auxiliar tenta registrar uma reposição sem informar o fornecedor
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "Fornecedor é obrigatório no registro de reposição"

  Cenário: Bloquear reposição com quantidade zero ou negativa
    Quando a auxiliar tenta registrar uma reposição com quantidade 0
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "A quantidade deve ser um valor positivo"

  Cenário: Bloquear reposição com custo unitário zero ou negativo
    Quando a auxiliar tenta registrar uma reposição com custo unitário R$ 0,00
    Então o sistema deve rejeitar o registro
    E exibir a mensagem "O custo unitário deve ser um valor positivo"

  Cenário: Sugerir quantidade com base na média de consumo
    Dado que o histórico de consumo de "Resina Composta" nos últimos 3 meses é de 15 unidades por mês
    Quando a auxiliar abre o formulário de reposição de "Resina Composta"
    Então o sistema deve sugerir a quantidade de 45 unidades como quantidade a repor
