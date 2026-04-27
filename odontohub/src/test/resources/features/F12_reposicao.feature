Funcionalidade: RelatÃ³rio de ReposiÃ§Ã£o de Materiais
    
      CenÃ¡rio: Realizar reposiÃ§Ã£o de material em estoque
        Dado que o material "Luva" estÃ¡ cadastrado com saldo de 10 unidades
        Quando a auxiliar registra a reposiÃ§Ã£o de 50 unidades de "Luva" do fornecedor "Dental Cremer" com custo unitÃ¡rio R$ 10.0
        Entao o saldo de "Luva" deve ser atualizado imediatamente para 60 unidades
        E a reposiÃ§Ã£o deve ser salva com fornecedor "Dental Cremer", quantidade 50 e custo total R$ 500.0
    
      CenÃ¡rio: Registrar lanÃ§amento financeiro de saÃ­da ao repor o material
        Dado que o material "Seringa" estÃ¡ cadastrado com saldo de 5 unidades
        Quando a auxiliar registra a reposiÃ§Ã£o de 20 unidades de "Seringa" com custo total de R$ 100.0
        Entao o saldo de "Seringa" deve ser atualizado imediatamente para 25 unidades
        E um lanÃ§amento de saÃ­da de R$ 100.0 deve ser criado automaticamente no fluxo de caixa
        E o lanÃ§amento deve ter categoria "ReposiÃ§Ã£o de Estoque"
      
      CenÃ¡rio: Tentar registrar reposiÃ§Ã£o sem fornecedor
        Dado que o material "Agulha" estÃ¡ cadastrado com saldo de 5 unidades
        Quando a auxiliar tenta registrar uma reposiÃ§Ã£o sem informar o fornecedor
        Entao o sistema deve rejeitar o registro
    
      CenÃ¡rio: Tentar registrar reposiÃ§Ã£o com quantidade negativa ou zero
        Dado que o material "Mascara" estÃ¡ cadastrado com saldo de 5 unidades
        Quando a auxiliar tenta registrar uma reposiÃ§Ã£o com quantidade 0
        Entao o sistema deve rejeitar o registro
      
      CenÃ¡rio: Tentar registrar reposiÃ§Ã£o com custo negativo
        Dado que o material "Touca" estÃ¡ cadastrado com saldo de 5 unidades
        Quando a auxiliar tenta registrar uma reposiÃ§Ã£o com custo unitÃ¡rio R$ -10.0
        Entao o sistema deve rejeitar o registro
    
      CenÃ¡rio: Sugerir quantidade de reposiÃ§Ã£o
        Dado que o material "Gaze" estÃ¡ cadastrado com saldo de 5 unidades
        E que o histÃ³rico de consumo de "Gaze" nos Ãºltimos 3 meses Ã© de 10 unidades por mÃªs
        Quando a auxiliar abre o formulÃ¡rio de reposiÃ§Ã£o de "Gaze"
        Entao o sistema deve sugerir a quantidade de 30 unidades como quantidade a repor
    
    
