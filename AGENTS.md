# AGENTS.md — OdontoHub

Arquivo de contexto para agentes de IA (Codex, etc.). **A fonte completa de
contexto deste projeto é o `CLAUDE.md` na raiz** — leia-o primeiro, junto com o
`README.md` e o `frontend/CLAUDE.md`. Este arquivo resume só o essencial.

## O que é
Sistema de gestão para clínicas odontológicas (projeto acadêmico de **DDD +
Arquitetura Limpa**). Backend Spring Boot (Java 21) + frontend React. 18
funcionalidades (F1–F18) divididas em bounded contexts, cada uma com dono e
testes BDD (Cucumber, em português).

## Como rodar
```bash
docker compose up --build      # na raiz
```
- Frontend: http://localhost:8090 (cai na tela de login)
- API:      http://localhost:8080/api/...
- Login demo: `admin@odontohub.com` / `odonto123`
- Backend sem Docker: `cd odontohub && ./mvnw spring-boot:run`
- Testes BDD: `cd odontohub && ./mvnw test` (hoje: **150 cenários verdes**)
- Frontend dev: `cd frontend && npm install && npm run dev`

## Arquitetura (siga ao criar/alterar)
Cada contexto em `odontohub/src/main/java/com/g4/odontohub/<contexto>/` tem 4 camadas:
`domain/` (model, service, repository=portas, event) · `application/` (serviço de
aplicação + publica eventos) · `infrastructure/` (adapters: InMemory p/ BDD e
`jpa/` p/ produção; `config/<Contexto>BeanConfig` é a raiz de composição) ·
`presentation/` (`*RestController.java` = fonte da verdade dos endpoints REST).
Banco: H2 em arquivo (ORM JPA/Hibernate), persistente no volume Docker.

## Como ligar uma tela ao backend (padrão já provado)
Referências prontas: `estoque/.../MaterialRestController.java` (GET `listar()`),
`estoque/.../EstoqueBeanConfig.java` (seed `CommandLineRunner`), e no frontend
`pages/auxiliar/Estoque.tsx` + `pages/dentista/Financeiro.tsx` (adapter +
`useEffect` + modal de mutação com refetch). Detalhes em `frontend/CLAUDE.md`.
Pegadinhas do JSON: id aninhado (`{"id":1}` → `b.id?.id`), enums SCREAMING_CASE,
dinheiro número puro, datas ISO.

## Regras do time (NÃO viole)
- Mexa apenas nos contextos da SUA funcionalidade; não altere o trabalho de outros.
- Não invente endpoints/campos — confirme no `*RestController.java` ou via
  `curl http://localhost:8080/api/...`.
- Toda feature `.feature` nova precisa ser registrada em `CucumberRunnerTest.java`.
- Trabalhe em **branch de feature**, nunca na `main`. Sem `push --force`.
- **NUNCA** adicione trailer de co-autoria de IA nos commits (nada de
  "Co-Authored-By", "Generated with...", nem assinatura de Codex/Claude/ChatGPT).
- Não rode `docker compose down -v` sem necessidade (apaga o banco dos colegas).

## Donos das funcionalidades (resumo)
F1 Agendamento, F6 Esterilização, F13 Cad. Pacientes, F14 Cad. Instrumentos — João Patriota ·
F2 Anamnese, F3 Plano de Tratamento — Felipe Andrade ·
F4 Fluxo de Caixa, F5 Estoque, F18 Cad./Login de Clínica — Antônio Augusto ·
F7 Recall, F12 Equipe — Daniel Andrade ·
F8 Medicamentos, F9 Inadimplência, F15 Pagamentos — Mateus Dornellas ·
F10 Follow-up, F11 Churn — Jarbas Esteves ·
F16 Lembretes, F17 Não Comparecimento — Gabriel Belo.
