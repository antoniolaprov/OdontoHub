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

## 📌 Status do Projeto

🚧 Em desenvolvimento

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

O sistema está sendo modelado com foco em **Domain-Driven Design (DDD)**.

### Subdomínios identificados:

* **Core Domain:** Gestão Clínica
* **Supporting Domain:** Agendamento
* **Supporting Domain:** Financeiro
* **Generic Domain:** Autenticação e Usuários

### Bounded Contexts:

* Atendimento
* Agenda
* Financeiro
* Usuários

📄 O arquivo **CML** encontra-se em:

```text id="w6f4yx"
OdontoHub\odontohub\docs\domain
```

---

## 🧪 Cenários de Teste BDD

Os cenários BDD foram escritos em **Gherkin** e estão disponíveis em:

```text id="g7j2da"
OdontoHub\odontohub\docs\bdd
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

* Spring Boot

### Banco de Dados

* MySQL

### Testes

* JUnit
* Cucumber

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