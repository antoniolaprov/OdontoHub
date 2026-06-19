# OdontoHub — Frontend (React + Vite)

Frontend exportado do Figma Make (React 18 + Vite + TypeScript + Radix/shadcn).
As telas vêm com **dados mockados**; a tarefa é **ligá-las à API REST do backend**.

## Como rodar
```bash
npm install
npm run dev          # http://localhost:5173
```
O backend (Spring Boot) precisa estar rodando em `http://localhost:8080`
(`cd ../odontohub && ./mvnw spring-boot:run` ou via Docker).

## Regra de ouro (para não ligar errado)
1. **Só use endpoints que existem de verdade.** A fonte da verdade são os
   controllers do backend: `../odontohub/src/main/java/com/g4/odontohub/**/presentation/*RestController.java`.
   **Não invente rotas nem campos.** Se faltar um endpoint (ex.: listar tudo),
   adicione-o no backend primeiro.
2. **Confira o JSON real** antes de mapear: `curl http://localhost:8080/api/<rota>`.
3. Substitua o mock pelos dados reais **mantendo os componentes e tipos da tela**.

## Arquitetura da integração
- **Proxy:** `vite.config.ts` encaminha `/api/*` → `localhost:8080` (sem CORS).
- **Cliente HTTP:** `src/app/api/client.ts` — use `api.get/post/put/del`.
- **Adaptador por tela:** o backend usa formatos próprios (DDD) que **não batem 1:1**
  com os tipos das telas. Cada tela traduz backend → tipo da UI numa função
  `adaptar<Algo>(...)`. Veja o exemplo já pronto em
  `src/app/pages/recepcionista/Recall.tsx` (busca `/recalls`, adapta e mantém o
  mock como fallback).

## Padrão para ligar uma tela nova
```tsx
import { useEffect } from "react";
import { api } from "../../api/client";

// dentro do componente, ao lado do useState(MOCK_X):
useEffect(() => {
  api.get<any[]>("/rota")
    .then((lista) => { if (lista?.length) setX(lista.map(adaptarX)); })
    .catch((e) => console.warn("Falha ao carregar:", e));
}, []);
```

## Pegadinhas dos formatos JSON (backend → UI)
- **IDs são value objects aninhados:** o id vem como `{"id": 1}`, não `1`.
  Use `b.id?.id`.
- **Enums vêm como string em SCREAMING_CASE** e nem sempre coincidem com os da UI.
  Ex.: prioridade do backend `ALTA/MEDIA/BAIXA` ≠ UI `CRITICO/ALTO/NORMAL/BAIXO`;
  status `NA_FILA/AGENDADO/CONVERTIDO/...`. Mapeie com um `Record<string, ...>`.
- **Dinheiro** é número puro (`500.0`); **datas** são ISO (`2026-06-18` ou
  `2026-06-18T15:00:00`).
- Campos que a UI mostra mas o backend não tem (ex.: telefone do paciente no
  Recall) → use um default (`"—"`) até existir endpoint que forneça.

## Status da integração (telas)
- ✅ **Login / Cadastro de Clínica** (`/login`) — `POST /api/clinicas` (cadastro) +
  `POST /api/clinicas/login` (autenticação, F18). Sessão em `localStorage`
  (`odontohub.clinica`). Conta demo: `admin@odontohub.com` / `odonto123`.
- ✅ **Recepcionista / Recall (F07)** — `GET /api/recalls` (leitura) +
  `POST /api/recalls/paciente/{paciente}/tentativa-detalhada` (escrita real:
  registrar contato e converter em agendamento via `resultado: RETORNO_AGENDADO`,
  que é a única forma de o backend marcar como `AGENDADO`).
- ✅ **Dentista / Financeiro** — `GET /api/fluxo-caixa/lancamentos` + `/saldo-atual`
- ✅ **Dentista / Equipe (F12)** — `GET /api/colaboradores` (leitura) +
  `POST /completo` (cadastro), `PUT /{nome}/dados` (telefone/e-mail — só esses
  dois campos são editáveis depois do cadastro), `POST /{nome}/status`,
  `POST /{nome}/disponibilidade`, `POST /{nome}/ausencia` (escrita real).
- ✅ **Dentista / Dashboard** — Pareto via `GET /api/churn/pareto-cancelamentos`
  (série mensal de churn e custo de cadeira vazia seguem mock — sem endpoint)
- ✅ **Dentista / Medicamentos** — `GET /api/medicamentos`
- ✅ **Dentista / Prontuários (Anamnese + Plano de Tratamento, F02/F03)** —
  `GET /api/pacientes` (lista), `GET/POST /api/prontuarios/anamnese/...`,
  `GET/POST/PUT/DELETE /api/prontuarios/planos/...` (procedimentos, encerrar,
  excluir). A aba Prescrições (F08) continua mock — fora de escopo do F02/F03.
- ✅ **Recepcionista / Agenda (F01)** — `GET /api/agendamentos` (lista, com paciente/dentista
  já resolvidos pelo backend) + `POST /api/agendamentos` (criar) e `/{id}/confirmar`,
  `/{id}/cancelar`, `/{id}/remarcar` (ações reais). O dropdown de paciente usa
  `GET /api/pacientes`; `inadimplente` por paciente/agendamento não existe via API
  (a ACL cross-context é só em memória) — default `false`.
- ✅ **Recepcionista / Pacientes (F13)** — `GET /api/pacientes` (lista) + `POST /api/pacientes`
  (completo), `POST /rapido`, `PUT /{nome}/campo` (edita nome/cpf/dataNascimento/telefone/
  email um por vez), `POST /{nome}/restringir`. `temPlano`/`inadimplente` não existem no
  contexto de Cadastro de Paciente — default `false`; editar o status para algo diferente
  de RESTRITO não é persistido (domínio só suporta a transição para RESTRITO).
- ✅ **Auxiliar / Esterilização (F06) e Instrumentos (F14)** — `GET /api/instrumentos`
  (lista geral, ativos e inativos) + `POST /api/instrumentos` (cadastro, individual ou KIT),
  `POST /{nome}/esterilizar`, `/contaminar`, `/desativar`, `POST /prazo-global` e
  `POST /categorias/{categoria}/prazo` (configuração de validade). Editar nome/categoria/
  código/tipo de um instrumento já cadastrado não é suportado pelo domínio — fica só local.

> Agenda, Pacientes, Esterilização e Instrumentos **não têm fallback pro mock**: lista
> vazia ou erro de API mostra a tela vazia de verdade (com mensagem de "carregando"
> enquanto o primeiro fetch não responde). É proposital — diferente do Recall, aqui
> não tem ambiguidade entre "é mock" e "é dado real". Se a tela aparecer vazia, é
> porque o backend não tem registros (ou está fora do ar), não porque virou mock.
- ⬜ **Recepcionista** — Pagamentos, Acordos
- ⬜ **Auxiliar** — Estoque

> A maioria das telas pendentes precisa de **novos GETs de listagem** no backend
> (`GET /api/<contexto>`). Padrão já provado em Recall/Financeiro/Equipe/Medicamentos:
> adicione o endpoint, escreva o `adaptar<X>()` e o `useEffect`.
