# 🦷 OdontoHub — Entregáveis

Projeto acadêmico de **DDD + Arquitetura Limpa** para gestão de clínicas
odontológicas. Este README reúne **apenas os entregáveis** avaliados.

---

## 🌍 Descrição do Domínio (Linguagem Onipresente)

O domínio do sistema está relacionado à **gestão de clínicas odontológicas**.

### Principais entidades do negócio:

* **Paciente**: pessoa que recebe atendimento odontológico.
* **Dentista**: profissional responsável pelos procedimentos clínicos.
* **Secretário(a)**: responsável por agenda e suporte administrativo.
* **Consulta**: atendimento agendado entre paciente e dentista.
* **Prontuário**: histórico clínico do paciente.
* **Procedimento**: tratamento realizado durante consultas.
* **Pagamento**: registro financeiro referente aos atendimentos.
* **Agenda**: calendário de horários disponíveis e ocupados.

🔗 Documento completo da descrição do domínio:
https://docs.google.com/document/d/1in_TcAc0lF9e5tBAOWedj4N48vXn65GTV9aAGn454JA/edit?usp=sharing

---

## 🗺️ Mapa de Histórias do Usuário

O projeto foi estruturado utilizando **User Story Mapping**, organizando
funcionalidades por jornadas e prioridades.

🔗 Acesse o mapa completo:
https://miro.com/app/board/uXjVGhatDtU=/?share_link_id=290223200342

---

## 🎨 Protótipos

Protótipos de baixa fidelidade desenvolvidos para validar fluxos e interfaces.

🔗 Figma:
https://www.figma.com/make/qvOk0BQkaCuL9nJy93onZA/Low-Fidelity-Wireframes-for-OdontoCare?t=5TOoNjostOZmjDQl-1&preview-route=%2Fagendamento

---

## 🧩 Modelagem de Domínio (Context Mapper)

O sistema é modelado com **Domain-Driven Design (DDD)**.

### Subdomínios identificados:

* **Core Domain:** Agendamento, Prontuário Clínico, Cadastro de Paciente
* **Supporting Domain:** Financeiro, Estoque, Relacionamento com Paciente, Medicamento, Pagamento
* **Generic Subdomain:** Equipe

### Bounded Contexts:

* **AgendamentoContext** — Agendamento de Consultas e Retornos (F1)
* **ProntuarioClinicoContext** — Anamnese (F2), Plano de Tratamento (F3), Prescrição (F8)
* **FinanceiroContext** — Fluxo de Caixa (F4), Inadimplência e Acordos (F9)
* **EstoqueContext** — Materiais Consumíveis (F5), Esterilização (F6), Instrumentos (F14)
* **RelacionamentoPacienteContext** — Recall (F7), Follow-up (F10), Churn (F11)
* **EquipeContext** — Gestão de Colaboradores (F12)
* **CadastroPacienteContext** — Cadastro de Pacientes (F13)
* **MedicamentoContext** — Catálogo de Medicamentos (F8)
* **PagamentoContext** — Pagamentos e Quitação de Débitos (F15)
* **ConfirmacaoContext** — Lembretes e Confirmação (F16), Não Comparecimento (F17)

📄 O arquivo **CML** encontra-se na raiz do repositório:

```text
OdontoHub.cml
```

---

## 🧱 Padrões de Projeto Implementados

> A coluna **"Dono"** segue o **dono da funcionalidade** (bounded context) onde o
> arquivo de cada padrão vive — independentemente de quem fez o commit no Git.
> Como os padrões são transversais, vários membros os aplicam, cada um no seu contexto.

**Mapa de autoria por bounded context (pacote):**

| Bounded Context (pacote) | Funcionalidade(s) | Dono |
|---|---|---|
| `agendamento` | F1 | João Patriota |
| `prontuarioclinico` | F2, F3 | Felipe Andrade |
| `medicamento` + `prescricao` | F8 | Mateus Dornellas |
| `financeiro` (Fluxo de Caixa) | F4 | Antônio Augusto |
| `financeiro` (Inadimplência/Acordos) | F9 | Mateus Dornellas |
| `estoque` (Materiais Consumíveis) | F5 | Antônio Augusto |
| `estoque` (Esterilização/Instrumentos) | F6, F14 | João Patriota |
| `relacionamentopaciente/recall` | F7 | Daniel Andrade |
| `relacionamentopaciente/followup` + `churn` | F10, F11 | Jarbas Esteves |
| `equipe` | F12 | Daniel Andrade |
| `cadastropaciente` | F13 | João Patriota |
| `pagamento` | F15 | Mateus Dornellas |
| `confirmacao` | F16, F17 | Gabriel Belo |

### 1. Repository (com Ports & Adapters / Arquitetura Hexagonal)
A camada de domínio define a **porta** (interface) de persistência; a infraestrutura provê os **adaptadores**. O domínio não conhece JPA.
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - Portas: `…/domain/repository/<Agregado>Repository.java` (ex.: `pagamento/domain/repository/PagamentoRepository.java`, `equipe/domain/repository/ColaboradorRepository.java`, `financeiro/domain/repository/LancamentoRepository.java`)

### 2. Adapter
Cada porta tem **dois adaptadores**: um em memória (usado pelos testes BDD) e um JPA (usado pela aplicação real).
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - Em memória: `…/infrastructure/persistence/InMemory<Agregado>Repository.java`
  - JPA: `…/infrastructure/persistence/jpa/Jpa<Agregado>Repository.java` + `SpringData<Agregado>Repository.java`

### 3. Data Mapper
Tradução entre o **modelo de domínio** e o **modelo de persistência** (entidade JPA), mantendo o domínio livre de anotações de ORM.
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - `…/infrastructure/persistence/jpa/<Agregado>JpaEntity.java` (métodos `fromDomain` / `toDomain`)
  - `…/infrastructure/persistence/jpa/*Embeddable.java` (coleções aninhadas, ex.: `ProcedimentoEmbeddable.java`, `LogAuditoriaEmbeddable.java`)

### 4. Observer / Publish-Subscribe (Domain Events)
Barramento de eventos de domínio: serviços de domínio **publicam** eventos; serviços de aplicação **assinam** para integrar contextos (ex.: Estoque e Pagamento alimentam o Fluxo de Caixa).
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - Barramento: `shared/DomainEventPublisher.java` (infraestrutura compartilhada)
  - Eventos: `pagamento/domain/event/PagamentoRegistrado.java` (Mateus, F15) · `relacionamentopaciente/recall/domain/event/RecallEscalonado.java` (Daniel, F7) · `confirmacao/lembrete/domain/event/ConsultaConfirmadaPeloPaciente.java` (Gabriel, F16) · `confirmacao/naocomparecimento/domain/event/FaltaRegistrada.java` (Gabriel, F17)
  - Assinantes (integrações entre contextos): `pagamento/application/PagamentoApplicationService.java` (Mateus, F15→F4) · `confirmacao/lembrete/application/LembreteApplicationService.java` (Gabriel, F16→F1) · `confirmacao/naocomparecimento/application/NaoComparecimentoApplicationService.java` (Gabriel, F17→F11) · `relacionamentopaciente/recall/application/RecallApplicationService.java` (Daniel, F7→F10)

### 5. Factory Method
Fábricas estáticas para **reconstituir** agregados a partir da persistência e para converter rótulos em enums.
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - `reconstituir(...)` em agregados: `pagamento/domain/model/Pagamento.java`, `equipe/domain/model/Colaborador.java`, `relacionamentopaciente/recall/domain/model/Recall.java`, `prontuarioclinico/domain/model/PlanoTratamento.java`
  - `fromLabel(...)` em enums: `equipe/domain/model/FuncaoColaborador.java`, `StatusColaborador.java`, `pagamento/domain/model/FormaPagamento.java`

### 6. Dependency Injection / Inversion of Control
A composição (porta → adaptador JPA) é feita por configuração Spring no **composition root**, e não com `new` espalhado.
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:**
  - `…/infrastructure/config/<Contexto>BeanConfig.java` (ex.: `pagamento/infrastructure/config/PagamentoBeanConfig.java`, `equipe/infrastructure/config/EquipeBeanConfig.java`)
  - `OdontoHubApplication.java`

### 7. Facade (Application Service)
Os serviços de aplicação oferecem uma **fachada** simples sobre os serviços de domínio e coordenam integrações entre contextos.
- **Implementado por:** o dono da funcionalidade no respectivo bounded context (ver mapa de autoria acima)
- **Arquivos:** `…/application/<Contexto>ApplicationService.java`

---

## 🧪 Cenários de Teste BDD

Os cenários BDD foram escritos em **Gherkin** (um arquivo `.feature` por
funcionalidade) e estão disponíveis em:

```text
odontohub/src/test/resources/features
```

---

## 🤖 Automação com Cucumber

Os cenários BDD foram automatizados utilizando:

* Java
* Spring Boot
* Cucumber
* JUnit

Executados via Maven Wrapper.

---

## 👥 Equipe e Autoria das Funcionalidades

| Membro | Funcionalidades |
|---|---|
| **João Patriota** | F1 – Agendamento de Consultas e Retornos · F6 – Status de Esterilização · F13 – Cadastro de Pacientes · F14 – Cadastro de Instrumentos |
| **Mateus Dornellas** | F8 – Cadastro e Gestão de Medicamentos · F9 – Gestão Ativa de Inadimplência e Acordos · F15 – Gestão de Pagamentos e Quitação de Débitos |
| **Jarbas Esteves** | F11 – Dashboard de Churn e Inteligência de Retenção · F10 – Execução de Protocolos de Pós-Operatório Ativo |
| **Daniel Andrade** | F7 – Automação e Fila de Recall · F12 – Gestão de Equipe e Colaboradores |
| **Felipe Andrade** | F2 – Registro de Anamnese · F3 – Gestão e Execução do Plano de Tratamento |
| **Antônio Augusto** | F4 – Fluxo de Caixa do Consultório · F5 – Controle de Estoque de Materiais Consumíveis |
| **Gabriel Belo** | F16 – Confirmação e Lembretes de Consulta · F17 – Registro de Não Comparecimento |
