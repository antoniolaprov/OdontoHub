# language: pt
Funcionalidade: Relatório de Reposição de Materiais
    
  Cenário: Realizar reposição de material em estoque
    Dado que o material "Luva" está cadastrado com saldo de 10 unidades
    Quando a auxiliar registra a reposição de 50 unidades de "Luva" do fornecedor "Dental Cremer" com custo unitário R$ 10.0
    Então o saldo de "Luva" deve ser atualizado imediatamente para 60 unidades
    E a reposição deve ser salva com fornecedor "Dental Cremer", quantidade 50 e custo total R$ 500.0
    
  Cenário: Registrar lançamento financeiro de saída ao repor o material
    Dado que o material "Seringa" está cadastrado com saldo de 5 unidades
    Quando a auxiliar registra a reposição de 20 unidades de "Seringa" com custo total de R$ 100.0
    Então o saldo de "Seringa" deve ser atualizado imediatamente para 25 unidades
    E um lançamento de saída de R$ 100.0 deve ser criado automaticamente no fluxo de caixa
    E o lançamento deve ter categoria "Reposição de Estoque"
      
  Cenário: Tentar registrar reposição sem fornecedor
    Dado que o material "Agulha" está cadastrado com saldo de 5 unidades
    Quando a auxiliar tenta registrar uma reposição sem informar o fornecedor
    Então o sistema deve rejeitar o registro
    
  Cenário: Tentar registrar reposição com quantidade negativa ou zero
    Dado que o material "Mascara" está cadastrado com saldo de 5 unidades
    Quando a auxiliar tenta registrar uma reposição com quantidade 0
    Então o sistema deve rejeitar o registro
      
  Cenário: Tentar registrar reposição com custo negativo
    Dado que o material "Touca" está cadastrado com saldo de 5 unidades
    Quando a auxiliar tenta registrar uma reposição com custo unitário R$ -10.0
    Então o sistema deve rejeitar o registro
    
  Cenário: Sugerir quantidade de reposição
    Dado que o material "Gaze" está cadastrado com saldo de 5 unidades
    E que o histórico de consumo de "Gaze" nos últimos 3 meses é de 10 unidades por mês
    Quando a auxiliar abre o formulário de reposição de "Gaze"
    Então o sistema deve sugerir a quantidade de 30 unidades como quantidade a repor