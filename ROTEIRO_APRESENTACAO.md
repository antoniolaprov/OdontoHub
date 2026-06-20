# 🎤 Roteiro de Apresentação — OdontoHub
### "A jornada de um paciente na Clínica OdontoHub"

> Apresentação **informal**, direto com o professor, **sem abrir código/IDE em nenhum
> momento** — só o sistema rodando no navegador e, no fechamento, um terminal com o
> resultado dos testes (texto em português, não código). Cada um fala da própria
> funcionalidade, mostrando na tela e contando a história, sem ler slide.

---

## ✅ Antes de começar
- Sistema rodando: `docker compose up --build` (faça o **1º build com antecedência** — baixa dependências e leva alguns minutos).
- Acesso: **http://localhost:8090** · login `admin@odontohub.com` / `odonto123`.
- Os dados já vêm **semeados** (paciente *Maria Santos*, equipe, estoque, medicamentos, etc.) — não precisa cadastrar nada do zero pra ter o que mostrar.
- Use o botão **"← Trocar perfil"** no menu lateral pra navegar entre **Recepcionista / Auxiliar / Cirurgião-Dentista** — é o que faz o "handoff" entre quem está falando.
- Garanta que a máquina está na branch `main` (ou `feat/automacao-bdd-f07-f17`, são iguais).
- Deixa um terminal aberto, na pasta `odontohub`, **sem rodar nada ainda** — só usado no fechamento.

> 💡 Três funcionalidades não têm tela própria (Follow-up F10, Lembretes F16, Não
> Comparecimento F17) — são regras que rodam "nos bastidores" (disparadas
> automaticamente pelo sistema, não por um clique do usuário). Em vez de abrir
> código pra mostrar isso, a gente **conta a regra com uma frase** e deixa a prova
> pro fechamento, quando todo mundo já apresentou.

---

## 1️⃣ Antônio Augusto — Abertura + Clínica (F18) + Financeiro (F4) + Estoque (F5)
*~4 min — abre a apresentação porque o contexto dele é "a própria clínica".*

- **Login/Cadastro de Clínica (F18):** mostra a tela de login, comenta rápido que o sistema é **multi-clínica** (cada clínica tem seu próprio login) e que a senha é protegida (não fica visível nem pra quem administra). Faz o login.
- **Auxiliar → Estoque (F5):** mostra os materiais cadastrados; clica em **Repor** num material que está baixo.
- **Cirurgião-Dentista → Fluxo de Caixa (F4):** mostra o **Saldo Atual** e os lançamentos.
  - **🔗 Plot twist:** "a reposição que eu acabei de fazer no estoque já apareceu aqui como uma saída" — mostra o lançamento novo. Depois faz um **Novo Lançamento Manual** pra fechar.

## 2️⃣ João Patriota — Recepção: Pacientes (F13) + Agenda (F1) + Instrumentos (F14) + Esterilização (F6)
*~5 min — "recebe" o paciente e cuida dos bastidores da clínica.*

- **Recepcionista → Pacientes (F13):** cadastra um paciente novo, mostrando que o sistema **formata sozinho** CPF, telefone e data de nascimento enquanto digita.
- **Agenda (F1):** abre o seletor de paciente e mostra que o paciente **que acabou de cadastrar já está lá** — cria um agendamento pra ele.
- **Auxiliar → Instrumentos (F14):** mostra a lista de instrumentos, cadastra um novo.
- **Auxiliar → Esterilização (F6):** pega um instrumento e roda o ciclo: esterilizar → (se quiser, mostrar) contaminar.

## 3️⃣ Felipe Andrade — Atendimento Clínico: Anamnese (F2) + Plano de Tratamento (F3)
*~4 min — o paciente é atendido.*

- **Cirurgião-Dentista → Prontuários:** escolhe um paciente.
- **Anamnese (F2):** preenche alergias e condições de saúde do paciente.
- **Plano de Tratamento (F3):** cria um plano, adiciona um procedimento (já com os **materiais usados**, que abate do estoque de verdade), marca como **Realizado** e por fim encerra o plano.
  - Comenta de passagem: cada alteração fica registrada num histórico — não tem como "sumir" com uma evolução clínica depois de 24h.

## 4️⃣ Mateus Dornellas — Medicação + Financeiro do Paciente: Medicamentos (F8) + Pagamentos (F15) + Inadimplência (F9)
*~5 min — prescrição e cobrança.*

- **Cirurgião-Dentista → Medicamentos (F8):** mostra o catálogo de medicamentos.
- **Recepcionista → Pagamentos (F15):** numa parcela em aberto, clica **Registrar pagamento → Confirmar agora**.
  - **🔗 Plot twist:** volta no Financeiro (F4) e mostra que esse pagamento **já entrou como receita no caixa**, sem ninguém ter lançado na mão.
- **Recepcionista → Acordos/Inadimplência (F9):** mostra um paciente **Restrito** (com juros calculados), registra uma cobrança e fecha um **acordo** — a restrição cai.

## 5️⃣ Daniel Andrade — Relacionamento + Equipe: Recall (F7) + Equipe (F12)
*~4 min.*

- **Recepcionista → Recall (F7):** mostra a fila de pacientes que estão devendo retorno, ordenada por prioridade; registra uma tentativa de contato com um deles.
- **Cirurgião-Dentista → Equipe (F12):** mostra os colaboradores e as permissões de cada função (Cirurgião-Dentista / Recepcionista / Auxiliar); cadastra um colaborador novo e registra uma ausência.

## 6️⃣ Jarbas Esteves — Retenção: Churn (F11) + Follow-up (F10)
*~3 min.*

- **Churn (F11):** **Cirurgião-Dentista → Dashboard** → mostra o gráfico de **Pareto de cancelamentos** (quais motivos mais cancelam consulta).
- **Follow-up (F10) — sem tela, conta a regra:** "depois de um procedimento, o sistema agenda sozinho um contato de acompanhamento — não é o profissional que precisa lembrar". Não precisa abrir nada — só essa frase, e segue.

## 7️⃣ Gabriel Belo — Confirmação: Lembretes (F16) + Não Comparecimento (F17)
*~3 min — sem tela, conta as regras.*

- **Lembretes (F16):** "o sistema dispara um lembrete antes da consulta, e registra se o paciente confirmou ou recusou."
- **Não Comparecimento (F17):** "se o paciente falta sem avisar, o sistema registra a falta — e se isso se repetir, ele entra como reincidente, o que pode bloquear novo agendamento (é o gancho com o F1, que o João mostrou no começo)."
- Fecha com: "essas duas e o Follow-up do Jarbas não têm uma tela própria de propósito — são regras que o sistema cumpre sozinho. A prova de que funcionam é a próxima parte."

## 🏁 Fechamento (Antônio Augusto, ou todos juntos)
*~2 min — a prova de que tudo (com tela ou sem tela) realmente funciona.*

- Roda no terminal, ao vivo: `cd odontohub && ./mvnw test`.
- Enquanto sobe, comenta: "isso aqui não é código, é o resultado: cada linha é uma regra de negócio escrita em português, sendo checada de verdade contra o sistema."
- Deixa aparecer o resultado final: **"190 Scenarios (190 passed)"** — esse número cobre as 18 funcionalidades, incluindo as 3 que não têm tela.
- Frase de fechamento: *"Um paciente cadastrado uma vez aparece na Agenda, no Recall, na Inadimplência e nos Pagamentos sem ninguém digitar de novo — porque os módulos conversam entre si por dentro do sistema."*

---

## 📌 Tabela-resumo (cola rápida)

| # | Integrante | Funcionalidades | Onde apresentar |
|---|---|---|---|
| 1 | **Antônio Augusto** | F18, F4, F5 | Login · Auxiliar→Estoque · Dentista→Fluxo de Caixa |
| 2 | **João Patriota** | F13, F1, F14, F6 | Recep→Pacientes/Agenda · Aux→Instrumentos/Esterilização |
| 3 | **Felipe Andrade** | F2, F3 | Dentista→Prontuários |
| 4 | **Mateus Dornellas** | F8, F15, F9 | Dentista→Medicamentos · Recep→Pagamentos/Acordos |
| 5 | **Daniel Andrade** | F7, F12 | Recep→Recall · Dentista→Equipe |
| 6 | **Jarbas Esteves** | F11, F10 | Dentista→Dashboard · (F10 só de boca) |
| 7 | **Gabriel Belo** | F16, F17 | Só de boca, prova no fechamento |

---

## 🧭 Dicas finais
- **Ordem dos perfis** segue a jornada: **Recepção → Auxiliar → Dentista**. O botão **"Trocar perfil"** facilita os *handoffs* entre quem está apresentando.
- Ao agir numa tela, **dê F5 depois** pra provar que persistiu (é banco H2 real, não é mock).
- **Aposta nas integrações** (estoque→caixa, pagamento→caixa, paciente→agenda) — é o que mais impressiona e mostra que o sistema não é um conjunto de telas soltas.
- Nenhum apresentador precisa abrir IDE ou arquivo de código — só o navegador, e um terminal único no fechamento.
- Login demo: `admin@odontohub.com` / `odonto123`.

---

## 🗺️ Mapa rápido das telas (caminho no menu)

**Recepcionista:** Agenda (F1) · Pacientes (F13) · Recall (F7) · Acordos/Inadimplência (F9) · Pagamentos (F15)
**Auxiliar:** Estoque (F5) · Esterilização (F6) · Instrumentos (F14)
**Cirurgião-Dentista:** Prontuários — Anamnese/Plano (F2/F3) · Financeiro (F4) · Equipe (F12) · Medicamentos (F8) · Dashboard/Churn (F11)
**Sem tela (contar de boca, provar no fechamento):** Follow-up (F10) · Lembretes (F16) · Não Comparecimento (F17)
