# 🦷 OdontoHub

Sistema de gestão para clínicas odontológicas focado em **organização de prontuários**, **agendamentos**, **controle administrativo** e **eficiência no atendimento ao paciente**.

> 📚 Projeto acadêmico desenvolvido para a disciplina de **Requisitos, Projeto de Software e Validação** – Cesar School.

---

## 🚀 Como Executar os Testes (Importante para Avaliação)

Para validar o projeto, o avaliador deverá seguir os passos abaixo:

### ✅ Pré-requisitos

* **Java 21+** instalado
* Git instalado (opcional para clonagem)

### ✅ Passo 1: Clonar o repositório

```bash id="r1q0hf"
git clone <URL_DO_REPOSITORIO>
```

### ✅ Passo 2: Acessar a pasta do projeto

```bash id="q4n6lp"
cd odontohub
```

### ✅ Passo 3: Executar os testes automatizados

### Windows

```bash id="e5f2ud"
.\mvnw.cmd test --no-transfer-progress
```

### Linux / Mac

```bash id="a2w8kc"
./mvnw test --no-transfer-progress
```

> Todos os testes unitários e cenários BDD automatizados serão executados automaticamente.

---

## 🐳 Executar com Docker

A aplicação pode ser executada localmente em um container, sem instalar Java ou Maven na máquina (apenas o Docker é necessário). Os arquivos estão em `odontohub/`.

### Opção A — Docker Compose (recomendado)

```bash
cd odontohub
docker compose up --build
```

A API sobe em **http://localhost:8080**. Para parar: `docker compose down`.

### Opção B — Docker puro

```bash
cd odontohub
docker build -t odontohub:latest .
docker run -d --name odontohub -p 8080:8080 odontohub:latest
```

Parar e remover: `docker rm -f odontohub`.

> O build é multi-stage: o estágio 1 (Maven + JDK 21) empacota o JAR com o frontend
> Vaadin já embutido; o estágio 2 (apenas JRE 21) executa o JAR. O banco H2 fica em
> arquivo dentro do volume `/app/data`, persistindo entre reinícios do container.

### Endpoints de exemplo (REST)

```bash
# Fluxo de caixa
curl http://localhost:8080/api/fluxo-caixa/lancamentos
# Fila priorizada de recall (F07)
curl http://localhost:8080/api/recalls/fila-priorizada
# Console do banco H2 (navegador): http://localhost:8080/h2-console
```

---

## 📌 Status do Projeto

✅ Backend completo — 15 funcionalidades (F1–F12, F15–F17) com domínio, infraestrutura (JPA),
aplicação e apresentação (REST), automatizadas por **108 cenários BDD** (todos verdes).

---

## 💡 Sobre o Projeto

O **OdontoHub** surgiu da necessidade de modernizar a administração de clínicas odontológicas, reduzindo processos manuais e melhorando a experiência de dentistas, secretários e pacientes.

A plataforma centraliza informações clínicas e administrativas em um único ambiente digital, permitindo maior organização, rastreabilidade e produtividade.

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

### Problemas identificados:

* Agendamentos desorganizados
* Dificuldade em localizar históricos clínicos
* Controle financeiro manual
* Falta de integração entre setores da clínica

### Solução proposta:

Criar uma plataforma web que permita gerenciar todos esses processos de forma simples, segura e eficiente.

🔗 Documento completo da descrição do domínio:
https://docs.google.com/document/d/1in_TcAc0lF9e5tBAOWedj4N48vXn65GTV9aAGn454JA/edit?usp=sharing

---

## 🗺️ Mapa de Histórias do Usuário

O projeto foi estruturado utilizando **User Story Mapping**, organizando funcionalidades por jornadas e prioridades.

🔗 Acesse o mapa completo:
https://miro.com/app/board/uXjVGhatDtU=/?share_link_id=290223200342

### Exemplos de histórias:

* Como secretário, quero agendar consultas para organizar a agenda da clínica.
* Como dentista, quero acessar o prontuário do paciente para consultar histórico clínico.
* Como administrador, quero visualizar pagamentos para controlar o financeiro.
* Como paciente, quero receber confirmação de consulta para evitar esquecimentos.

---

## 🎨 Protótipos

Protótipos de baixa fidelidade foram desenvolvidos para validar fluxos e interfaces.

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
* **EstoqueContext** — Materiais Consumíveis (F5), Esterilização (F6), Instrumentos (F16)
* **RelacionamentoPacienteContext** — Recall (F7), Follow-up (F10), Churn (F11)
* **EquipeContext** — Gestão de Colaboradores (F12)
* **CadastroPacienteContext** — Cadastro de Pacientes (F15)
* **MedicamentoContext** — Catálogo de Medicamentos (F8)
* **PagamentoContext** — Pagamentos e Quitação de Débitos (F17)

📄 O arquivo **CML** encontra-se na raiz do repositório:

```text id="w6f4yx"
OdontoHub.cml
```

---

## 🧪 Cenários de Teste BDD

Os cenários BDD foram escritos em **Gherkin** (um arquivo `.feature` por funcionalidade) e estão disponíveis em:

```text id="g7j2da"
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

## 🛠️ Tecnologias Utilizadas

### Backend

* Spring Boot (Spring Web, Spring Data JPA)
* Java 21+

### Persistência

* JPA / Hibernate
* H2 (banco em arquivo, durável)

### Testes

* JUnit (Platform Suite)
* Cucumber (BDD em Gherkin, `# language: pt`)

### Protótipos

* Figma

### Planejamento

* Miro

---

## 👥 Equipe

* João Patriota
* Mateus Dornellas
* Jarbas Esteves
* Daniel Andrade
* Felipe Andrade
* Antônio Augusto
* Gabriel Belo

---

## 📌 Instituição

Projeto desenvolvido na **Cesar School**.