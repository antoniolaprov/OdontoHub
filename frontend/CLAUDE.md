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
- ✅ **Recepcionista / Recall** — `GET /api/recalls`
- ✅ **Dentista / Financeiro** — `GET /api/fluxo-caixa/lancamentos` + `/saldo-atual`
- ✅ **Dentista / Equipe** — `GET /api/colaboradores`
- ✅ **Dentista / Dashboard** — Pareto via `GET /api/churn/pareto-cancelamentos`
  (série mensal de churn e custo de cadeira vazia seguem mock — sem endpoint)
- ✅ **Dentista / Medicamentos** — `GET /api/medicamentos`
- ⬜ **Dentista / Prontuários** — precisa de **read model novo no backend**
  (lista de pacientes/planos/procedimentos/prescrições/ficha clínica não existe).
- ⬜ **Recepcionista** — Agenda, Pacientes, Pagamentos, Acordos
- ⬜ **Auxiliar** — Estoque, Esterilização, Instrumentos

> A maioria das telas pendentes precisa de **novos GETs de listagem** no backend
> (`GET /api/<contexto>`). Padrão já provado em Recall/Financeiro/Equipe/Medicamentos:
> adicione o endpoint, escreva o `adaptar<X>()` e o `useEffect`.
