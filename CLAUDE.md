# OdontoHub — contexto do projeto (leia antes de mexer)

Sistema de gestão para clínicas odontológicas, feito como projeto acadêmico de
**DDD + Arquitetura Limpa**. Backend em Spring Boot (Java 21) e frontend React.
Este arquivo é lido automaticamente pelo Claude Code — use-o como ponto de partida.

## O que é o sistema
Plataforma para clínicas com 18 funcionalidades (F1–F18) divididas em **bounded
contexts** (DDD). Cada funcionalidade tem um **dono** (integrante da equipe) e é
automatizada por testes **BDD (Cucumber, em português)**.

## Como rodar (Docker — sobe tudo com um comando)
```bash
docker compose up --build      # na raiz do repo
```
- Frontend (app):  http://localhost:8090  → cai na **tela de login**
- API (direta):    http://localhost:8080/api/...
- Login demo:      `admin@odontohub.com` / `odonto123`
- Parar:           `docker compose down`  (use `-v` para zerar o banco H2)

Para rodar sem Docker: backend `cd odontohub && ./mvnw spring-boot:run`;
frontend `cd frontend && npm install && npm run dev` (porta 5173, com proxy p/ 8080).

## Stack
- **Backend:** Spring Boot 4, Java 21, Spring Data JPA, Hibernate, H2 em arquivo
  (`jdbc:h2:file:./data/odontohub`), BCrypt (spring-security-crypto). Vaadin é só
  scaffolding — **ignore** `odontohub/src/main/frontend/` (não versionado).
- **Testes:** Cucumber 7.18 (BDD, `# language: pt`) + JUnit Platform Suite.
- **Frontend:** React 18 + Vite + TypeScript + Radix/shadcn. Veja `frontend/CLAUDE.md`.

## Arquitetura (siga este padrão ao criar/alterar código)
Cada contexto em `odontohub/src/main/java/com/g4/odontohub/<contexto>/` tem 4 camadas:
- `domain/model` — agregados, value objects (IDs são `record`), enums.
- `domain/service` — regras de negócio; `domain/repository` — **portas** (interfaces);
  `domain/event` — eventos de domínio (`record`).
- `application` — serviço de aplicação que orquestra domínio + publica eventos.
- `infrastructure` — adapters: `InMemory<...>Repository` (usado pelo BDD) e
  `jpa/` (produção); `config/<Contexto>BeanConfig.java` é a raiz de composição.
- `presentation` — `*RestController.java` (a **fonte da verdade** dos endpoints REST).

**Ports & Adapters / dual adapter:** o mesmo repositório-porta tem duas implementações
(in-memory p/ testes, JPA p/ produção). Eventos usam `shared/DomainEventPublisher`
(bus estático publish/subscribe); as inscrições ficam só nos construtores de produção
(BeanConfig), para não vazar entre testes BDD.

Um bom contexto-modelo para copiar: **`cadastropaciente`** (ou `clinica`, o mais recente).

## Funcionalidades e donos (F1–F18)
F1 Agendamento (João Patriota) · F2 Anamnese (Felipe Andrade) · F3 Plano de Tratamento
(Felipe Andrade) · F4 Fluxo de Caixa (Antônio Augusto) · F5 Estoque (Antônio Augusto) ·
F6 Esterilização (João Patriota) · F7 Recall (Daniel Andrade) · F8 Medicamentos
(Mateus Dornellas) · F9 Inadimplência (Mateus Dornellas) · F10 Follow-up (Jarbas Esteves) ·
F11 Churn (Jarbas Esteves) · F12 Equipe (Daniel Andrade) · F13 Cadastro Pacientes
(João Patriota) · F14 Cadastro Instrumentos (João Patriota) · F15 Pagamentos
(Mateus Dornellas) · F16 Lembretes (Gabriel Belo) · F17 Não Comparecimento (Gabriel Belo) ·
F18 Cadastro/Login de Clínica (Antônio Augusto).

## BDD (regras importantes)
- Features: `odontohub/src/test/resources/features/F<NN>_*.feature` (um por funcionalidade).
- Steps: `odontohub/src/test/java/com/g4/odontohub/steps/`.
- A glue é **global**: o texto de cada step precisa ser **único em todo o projeto**,
  senão dá `DuplicateStepDefinition`/`Ambiguous`. Ao criar steps de um contexto novo,
  use frases específicas (ex.: "...da clínica ...").
- **Toda feature nova precisa ser registrada** em `CucumberRunnerTest.java`
  (`@SelectClasspathResource`), senão ela não roda.
- Rodar os testes: `cd odontohub && ./mvnw test`. Hoje: **150 cenários, todos verdes.**

## Pegadinhas que já nos morderam
- Enums em H2 durável: ao **adicionar valor a um enum**, o `ddl-auto=update` não
  atualiza o CHECK constraint → apague a pasta `data/` (ou `docker compose down -v`).
- `vite build`/esbuild **não faz type-check** — o build pode passar com tipos frouxos.
- IDs no JSON do backend vêm aninhados (`{"id": 1}`, não `1`); enums em SCREAMING_CASE.

## Regras de trabalho (combinadas com a equipe — NÃO viole)
- **Nunca** adicione `Co-Authored-By: Claude` nem "Generated with Claude Code" nos commits.
- Trabalhe sempre em **branch de feature**, nunca direto na `main`.
- Não invente endpoints/campos no frontend: confira em `*RestController.java` ou
  `curl http://localhost:8080/api/...`. Detalhes em `frontend/CLAUDE.md`.

## Mais contexto
- `README.md` — visão geral, seção de **padrões de projeto** (quem implementou + arquivos).
- `frontend/CLAUDE.md` — como ligar telas à API (adapter + useEffect + fallback).
- `OdontoHub.cml` — especificação Context Mapper dos bounded contexts.
