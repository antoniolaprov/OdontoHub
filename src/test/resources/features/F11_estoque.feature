# language: pt
Funcionalidade: F11 - Controle de Estoque de Materiais Consumíveis
  Como Auxiliar
  Eu quero que o sistema desconte automaticamente os materiais utilizados em cada procedimento e me alerte quando o estoque estiver baixo
  Para que o consultório nunca fique sem insumos essenciais

  Contexto:
    Dado que o material "Luva Descartável" está cadastrado com saldo de 50 unidades e ponto mínimo de 20

  Cenário: Realizar baixa automática de estoque ao confirmar procedimento
    Dado que o procedimento "Extração" utiliza 2 unidades de "Luva Descartável"
    Quando o dentista registra o procedimento "Extração" como realizado
    Então o saldo de "Luva Descartável" deve ser reduzido de 50 para 48 unidades automaticamente

  Cenário: Bloquear procedimento com estoque insuficiente
    Dado que o material "Anestésico" tem saldo de 1 unidade
    E que o procedimento "Extração" requer 3 unidades de "Anestésico"
    Quando o dentista tenta registrar o procedimento "Extração" como realizado
    Então o sistema deve bloquear o registro
    E exibir a mensagem "Estoque insuficiente de Anestésico: disponível 1, necessário 3"

  Cenário: Emitir alerta ao atingir ponto mínimo de estoque
    Dado que o material "Luva Descartável" tem saldo de 22 unidades e ponto mínimo de 20
    E que o procedimento "Extração" utiliza 3 unidades de "Luva Descartável"
    Quando o dentista registra o procedimento "Extração" como realizado
    Então o saldo de "Luva Descartável" deve ser reduzido para 19 unidades
    E o sistema deve emitir um alerta ao dentista informando estoque abaixo do ponto mínimo

  Cenário: Não emitir alerta quando saldo permanece acima do ponto mínimo
    Dado que o material "Luva Descartável" tem saldo de 50 unidades e ponto mínimo de 20
    E que o procedimento "Limpeza" utiliza 2 unidades de "Luva Descartável"
    Quando o dentista registra o procedimento "Limpeza" como realizado
    Então o saldo de "Luva Descartável" deve ser 48
    E nenhum alerta de estoque deve ser emitido

  Cenário: Dentista configura ponto mínimo de material
    Dado que o material "Resina" está cadastrado sem ponto mínimo definido
    Quando o dentista define o ponto mínimo de "Resina" como 10 unidades
    Então o ponto mínimo de "Resina" deve ser salvo como 10 unidades

  Esquema do Cenário: Baixa de múltiplos materiais ao realizar procedimento
    Dado que o procedimento "<procedimento>" utiliza:
      | Material   | Quantidade |
      | Luva       | 2          |
      | Anestésico | 1          |
    Quando o dentista registra o procedimento "<procedimento>" como realizado
    Então todos os materiais devem ter seus saldos reduzidos conforme o consumo

    Exemplos:
      | procedimento |
      | Extração     |
      | Restauração  |
