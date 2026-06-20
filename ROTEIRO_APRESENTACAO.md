# 🎤 Roteiro de Apresentação — OdontoHub
### "A jornada de um paciente na Clínica OdontoHub"

> Sistema de gestão para clínicas odontológicas — **DDD + Arquitetura Limpa**.
> 18 funcionalidades (F1–F18) em *bounded contexts* independentes, que se
> conversam por **eventos de domínio**, com cobertura **BDD (Cucumber, 190 cenários verdes)**.

---

## ✅ Antes de começar
- Sistema rodando: `docker compose up --build` (faça o **1º build com antecedência** — baixa dependências e leva alguns minutos).
- Acesso: **http://localhost:8090** · login `admin@odontohub.com` / `odonto123`.
- Os dados já vêm **semeados** (paciente *Maria Santos*, equipe, estoque, medicamentos, etc.).
- Use o botão **"← Trocar perfil"** no menu lateral pra navegar entre **Recepcionista / Auxiliar / Cirurgião-Dentista**.
- **Importante:** garanta que a máquina está na branch `feat/automacao-bdd-f07-f17`.

> 💡 As funcionalidades **com tela** são demonstradas ao vivo. As que são
> **backend + regra de negócio** (Follow-up F10, Lembretes F16, Não Comparecimento F17)
> são apresentadas pelos **cenários BDD em português** (especificação executável) + testes.

---

## 1️⃣ Antônio Augusto — Abertura + Clínica (F18) + Financeiro (F4) + Estoque (F5)
*~4 min — abre a apresentação porque o contexto dele é "a própria clínica".*

- **Login/Cadastro de Clínica (F18):** mostra a tela de login; explica que é **multi-clínica**, com senha protegida por **BCrypt**. Faz o login.
- **Pitch de arquitetura (30s):** *"O sistema é DDD + Arquitetura Limpa, 18 funcionalidades em bounded contexts independentes, que se conversam por eventos de domínio."*
- **Auxiliar → Estoque (F5):** mostra os materiais; clica **Repor** num material.
- **Cirurgião-Dentista → Fluxo de Caixa (F4):** mostra o **Saldo Atual** e os lançamentos.
  - **🔗 Integração:** repor estoque gerou automaticamente uma **saída no caixa** (eventos entre contextos). Faz um **Novo Lançamento Manual**.

## 2️⃣ João Patriota — Recepção: Pacientes (F13) + Agenda (F1) + Instrumentos (F14) + Esterilização (F6)
*~5 min — "recebe" o paciente e cuida dos bastidores.*

- **Recepcionista → Pacientes (F13):** cadastra um paciente novo — mostra as **máscaras automáticas** (CPF / telefone / data de nascimento).
- **🔗 Integração ao vivo:** vai na **Agenda (F1)** e mostra que o paciente recém-cadastrado **já aparece no seletor** (propagação por eventos F13→F1). Cria um **agendamento**.
- **Auxiliar → Instrumentos (F14):** lista / cadastra um instrumento.
- **Auxiliar → Esterilização (F6):** roda um **ciclo de esterilização** num instrumento (esterilizar → contaminar).

## 3️⃣ Felipe Andrade — Atendimento Clínico: Anamnese (F2) + Plano de Tratamento (F3)
*~4 min — o paciente é atendido.*

- **Cirurgião-Dentista → Prontuários:** escolhe o paciente.
- **Anamnese (F2):** preenche alergias e condições sistêmicas.
- **Plano de Tratamento (F3):** cria o plano, **adiciona um procedimento**, marca como **Realizado** e por fim **Encerra o plano**.
  - Destaque: **trilha de auditoria** e **versionamento** do plano.

## 4️⃣ Mateus Dornellas — Medicação + Financeiro do Paciente: Medicamentos (F8) + Pagamentos (F15) + Inadimplência (F9)
*~5 min — prescrição e cobrança.*

- **Cirurgião-Dentista → Medicamentos (F8):** mostra o catálogo (classes farmacológicas validadas).
- **Recepcionista → Pagamentos (F15):** numa parcela em aberto, clica **Registrar pagamento → Confirmar agora**.
  - **🔗 Integração:** o recebimento **gera entrada automática no Fluxo de Caixa** (pode voltar no F4 pra mostrar).
- **Recepcionista → Acordos/Inadimplência (F9):** mostra o paciente **Restrito** (juros/multa calculados), **registra uma cobrança** e **cria um acordo** (a restrição some).

## 5️⃣ Daniel Andrade — Relacionamento + Equipe: Recall (F7) + Equipe (F12)
*~4 min.*

- **Recepcionista → Recall (F7):** mostra a **fila de retorno** priorizada; **registra uma tentativa de contato** com um paciente.
- **Cirurgião-Dentista → Equipe (F12):** mostra os colaboradores e **permissões por função** (Especialista / Recepcionista / Auxiliar / Administrador); cadastra um colaborador, registra **disponibilidade / ausência**.

## 6️⃣ Jarbas Esteves — Retenção: Churn (F11) + Follow-up (F10)
*~3 min — parte visual + parte BDD.*

- **Churn (F11) — visual:** **Cirurgião-Dentista → Dashboard** → mostra o **Pareto de cancelamentos** (backend `/api/churn/pareto-cancelamentos`).
- **Follow-up (F10) — via BDD:** funcionalidade de regra (sem tela). Abre **`odontohub/src/test/resources/features/F10_followup.feature`** (Cucumber em português) e lê 1–2 cenários como "especificação executável". Explica o gatilho de follow-up pós-procedimento.

## 7️⃣ Gabriel Belo — Confirmação: Lembretes (F16) + Não Comparecimento (F17)
*~3 min — via BDD.*

- Funcionalidades de domínio/regra (sem tela dedicada). Apresenta pelos **cenários BDD**:
  - **`F16_lembretes.feature`** — envio / confirmação / recusa de lembrete de consulta.
  - **`F17_nao_comparecimento.feature`** — registro de falta e **detecção de paciente reincidente**.
- Mostra que a regra está **coberta e testada**, mesmo sem UI.

## 🏁 Fechamento (Antônio Augusto, ou todos juntos)
*~2 min — prova de qualidade + arquitetura.*

- Roda no terminal: `cd odontohub && ./mvnw test` → **"190 cenários, 0 falhas"** ao vivo.
  - *"Todas as funcionalidades têm comportamento garantido por testes automatizados (BDD)."*
- Frase de efeito sobre **integração:** *"Cada contexto é independente, mas o sistema é coeso — um paciente cadastrado uma vez aparece na Agenda, no Recall, na Inadimplência e nos Pagamentos, via eventos de domínio."*

---

## 📌 Tabela-resumo (cola rápida)

| # | Integrante | Funcionalidades | Onde apresentar |
|---|---|---|---|
| 1 | **Antônio Augusto** | F18, F4, F5 | Login · Auxiliar→Estoque · Dentista→Fluxo de Caixa |
| 2 | **João Patriota** | F13, F1, F14, F6 | Recep→Pacientes/Agenda · Aux→Instrumentos/Esterilização |
| 3 | **Felipe Andrade** | F2, F3 | Dentista→Prontuários |
| 4 | **Mateus Dornellas** | F8, F15, F9 | Dentista→Medicamentos · Recep→Pagamentos/Acordos |
| 5 | **Daniel Andrade** | F7, F12 | Recep→Recall · Dentista→Equipe |
| 6 | **Jarbas Esteves** | F11, F10 | Dentista→Dashboard · `.feature` (BDD) |
| 7 | **Gabriel Belo** | F16, F17 | `.feature` (BDD) |

---

## 🧭 Dicas finais
- **Ordem dos perfis** segue a jornada: **Recepção → Auxiliar → Dentista**. O botão **"Trocar perfil"** facilita os *handoffs* entre apresentadores.
- Ao agir numa tela, **dê F5 depois** pra provar que **persistiu** (não é mock — é banco H2 real).
- **Mencionem as integrações** (estoque→caixa, pagamento→caixa, paciente→agenda) — é o que mais impressiona e mostra maturidade de arquitetura.
- Login demo: `admin@odontohub.com` / `odonto123`.

---

## 🗺️ Mapa rápido das telas (caminho no menu)

**Recepcionista:** Agenda (F1) · Pacientes (F13) · Recall (F7) · Acordos/Inadimplência (F9) · Pagamentos (F15)
**Auxiliar:** Estoque (F5) · Esterilização (F6) · Instrumentos (F14)
**Cirurgião-Dentista:** Prontuários — Anamnese/Plano (F2/F3) · Financeiro (F4) · Equipe (F12) · Medicamentos (F8) · Dashboard/Churn (F11)
**Sem tela (apresentar via BDD):** Follow-up (F10) · Lembretes (F16) · Não Comparecimento (F17)
