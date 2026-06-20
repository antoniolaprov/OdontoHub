# 🗺️ Mapa de Histórias de Usuário — OdontoHub

Story map das 18 funcionalidades (F1–F18), organizado pelas
**jornadas de usuário** (personas) do sistema.


## 🟦 Persona: Recepcionista

### F13 · Cadastrar pacientes

| Cadastrar paciente | Cadastrar paciente (rápido) | Atualizar dados do paciente | Restringir / reativar paciente |
|---|---|---|---|
| Informar nome | Informar nome | Selecionar paciente | Selecionar paciente |
| Informar CPF | Informar telefone | Escolher campo a alterar | Confirmar alteração de status |
| Informar telefone | Confirmar cadastro rápido | Informar novo valor | |
| Informar data de nascimento | | Informar responsável pela alteração | |
| Informar e-mail | | Confirmar atualização | |
| Confirmar cadastro | | | |

### F1 · Agendar consultas

| Agendar consulta | Confirmar agendamento | Remarcar agendamento | Cancelar agendamento |
|---|---|---|---|
| Selecionar paciente | Selecionar agendamento | Selecionar agendamento | Selecionar agendamento |
| Selecionar dentista | Confirmar presença do paciente | Escolher nova data e horário | Selecionar categoria do motivo |
| Escolher data e horário | | Confirmar remarcação | Informar o motivo do cancelamento |
| Confirmar agendamento | | | Confirmar cancelamento |

### F16 · Enviar lembrete de consulta

| Gerar lembrete |
|---|
| Selecionar o agendamento |
| Escolher o canal de envio (WhatsApp/SMS/E-mail) |
| Confirmar a geração do lembrete |

### F17 · Registrar não comparecimento

| Registrar falta |
|---|
| Selecionar o agendamento |
| Informar se a falta foi justificada |
| Confirmar o registro da falta |

### F7 · Gerenciar fila de recall

| Consultar fila priorizada | Registrar tentativa de contato | Agendar a partir do recall |
|---|---|---|
| Abrir a fila de recall | Selecionar paciente na fila | Selecionar paciente na fila |
| Visualizar a prioridade de cada paciente | Escolher o canal de contato | Escolher data e horário |
| | Registrar o resultado do contato | Confirmar o agendamento |

### F9 · Gerir inadimplência e acordos

| Consultar inadimplentes | Registrar cobrança | Criar acordo de pagamento | Cancelar acordo |
|---|---|---|---|
| Abrir a lista de inadimplentes | Selecionar paciente inadimplente | Selecionar as parcelas vencidas | Selecionar o acordo ativo |
| Visualizar parcelas, juros e multa | Escolher o canal de contato | Definir o número de parcelas | Informar a justificativa |
| | Registrar o resultado da cobrança | Informar a justificativa | Confirmar o cancelamento |
| | | Confirmar o acordo | |

### F15 · Registrar pagamentos

| Registrar pagamento | Marcar aguardando comprovante | Confirmar comprovante | Cancelar pagamento pendente | Emitir comprovante / declaração |
|---|---|---|---|---|
| Selecionar a parcela em aberto | Selecionar a parcela | Selecionar a parcela pendente | Selecionar o lançamento pendente | Selecionar o paciente |
| Informar o valor recebido | Confirmar a marcação | Informar o valor recebido | Informar a justificativa | Consultar o histórico financeiro |
| Selecionar a forma de pagamento | | Informar a data | Confirmar o cancelamento | Solicitar comprovante ou declaração de quitação |
| Confirmar o pagamento | | Confirmar o recebimento | | |

### F10 · Acompanhar pós-operatório

| Preencher checklist de acompanhamento |
|---|
| Selecionar a tarefa de follow-up pendente |
| Informar se houve sangramento |
| Informar o nível de dor |
| Confirmar o checklist |

---

## 🟩 Persona: Auxiliar

### F5 · Controlar estoque de materiais

| Cadastrar material | Registrar reposição |
|---|---|
| Informar o nome do material | Selecionar o material |
| Informar a unidade de medida | Informar a quantidade reposta |
| Informar o saldo inicial | Informar o custo unitário |
| Informar o ponto mínimo | Informar o fornecedor |
| Confirmar o cadastro | Confirmar a reposição |

### F6 · Esterilizar instrumentos

| Marcar como estéril | Marcar como contaminado | Consultar instrumentos prontos para uso |
|---|---|---|
| Selecionar o instrumento | Selecionar o instrumento | Abrir a lista de instrumentos |
| Selecionar o responsável pela esterilização | Confirmar a marcação como contaminado | Filtrar por estéril e dentro do prazo |
| Confirmar a esterilização | | |

### F14 · Cadastrar instrumentos

| Cadastrar instrumento | Cadastrar kit | Desativar / reativar instrumento | Configurar prazo de validade |
|---|---|---|---|
| Informar o nome | Informar o nome do kit | Selecionar o instrumento | Escolher prazo global ou por categoria |
| Informar a categoria | Informar a categoria | Confirmar a alteração de status | Informar o novo prazo em dias |
| Informar o código | Informar o código | | Confirmar a alteração |
| Informar o prazo de validade | Selecionar os instrumentos que compõem o kit | | |
| Confirmar o cadastro | Confirmar o cadastro | | |

---

## 🟨 Persona: Cirurgião-Dentista

### F2 · Registrar anamnese

| Registrar anamnese | Atualizar anamnese |
|---|---|
| Selecionar o paciente | Selecionar o paciente |
| Selecionar o responsável | Adicionar nova alergia ou condição |
| Informar alergias | Confirmar a atualização |
| Informar condições sistêmicas | |
| Confirmar o registro | |

### F3 · Montar plano de tratamento

| Criar plano de tratamento | Adicionar procedimento | Realizar procedimento | Cancelar procedimento | Excluir procedimento | Encerrar plano |
|---|---|---|---|---|---|
| Selecionar o paciente | Selecionar o plano | Selecionar o procedimento pendente | Selecionar o procedimento pendente | Selecionar o procedimento realizado | Selecionar o plano |
| Confirmar a criação do plano | Informar o procedimento | Selecionar o agendamento vinculado | Informar a justificativa | Informar a justificativa | Informar a justificativa de encerramento |
| | Confirmar a inclusão | Selecionar o executor | Confirmar o cancelamento | Confirmar a exclusão | Confirmar o encerramento |
| | | Informar a evolução clínica | | | |
| | | Informar os materiais utilizados | | | |
| | | Confirmar a realização | | | |

### F4 · Acompanhar fluxo de caixa

| Registrar lançamento manual | Consultar saldo e lançamentos |
|---|---|
| Escolher o tipo (entrada ou saída) | Abrir o fluxo de caixa |
| Informar o valor | Filtrar por tipo |
| Informar a categoria | Filtrar por período |
| Informar a descrição | Visualizar o saldo atual |
| Confirmar o lançamento | |

### F11 · Analisar churn

| Consultar dashboard de cancelamentos | Filtrar churn por procedimento |
|---|---|
| Abrir o dashboard | Selecionar o procedimento |
| Visualizar o gráfico de Pareto dos motivos | Visualizar os pacientes evadidos filtrados |

### F12 · Gerir equipe

| Cadastrar colaborador | Editar dados do colaborador | Desativar / reativar colaborador | Definir disponibilidade | Registrar ausência |
|---|---|---|---|---|
| Informar o nome | Selecionar o colaborador | Selecionar o colaborador | Selecionar o colaborador | Selecionar o colaborador |
| Informar o CPF | Editar telefone ou e-mail | Confirmar a alteração de status | Informar os dias e horários disponíveis | Selecionar o tipo de ausência |
| Informar o telefone | Confirmar a atualização | | Confirmar a disponibilidade | Informar o período |
| Selecionar a função | | | | Confirmar o registro |
| Confirmar o cadastro | | | | |

### F8 · Consultar e prescrever medicamentos

| Consultar catálogo | Prescrever medicamento | Repetir prescrição anterior | Consultar histórico de prescrições |
|---|---|---|---|
| Abrir o catálogo de medicamentos | Selecionar o paciente | Selecionar a prescrição anterior | Selecionar o paciente |
| Selecionar o medicamento | Selecionar o medicamento | Confirmar a repetição | Filtrar por período |
| Visualizar contraindicações e interações | Ajustar a posologia | | Visualizar o histórico |
| | Adicionar observação | | |
| | Confirmar a prescrição | | |

### F18 · Cadastrar e acessar a clínica

| Cadastrar clínica | Acessar a clínica (login) |
|---|---|
| Informar nome da clínica | Informar e-mail cadastrado |
| Informar CNPJ | Informar senha |
| Informar e-mail | Confirmar login |
| Definir senha de acesso | |
| Confirmar cadastro | |

---
---

## 📌 Tabela-resumo (cola rápida)

| Persona | Funcionalidades |
|---|---|
| **Recepcionista** | F13, F1, F16, F17, F7, F9, F15, F10 |
| **Auxiliar** | F5, F6, F14 |
| **Cirurgião-Dentista** | F2, F3, F4, F11, F12, F8, F18 |
